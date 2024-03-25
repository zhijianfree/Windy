package com.zj.master.dispatch.task;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.DispatchType;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.feature.ExecutorUnit;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.master.dispatch.feature.FeatureDispatch;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import com.zj.master.entity.vo.ExecuteContext;
import com.zj.plugin.loader.ParameterDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class FeatureExecuteProxy implements IStopEventListener {

  public static final String TASK_FEATURE_TIPS = "no task need run";
  private final Map<String, FeatureTask> featureTaskMap = new ConcurrentHashMap<>();

  private final RequestProxy requestProxy;
  private final Executor executorService;
  private final TaskEndProcessor taskEndProcessor;
  private final IExecutePointRepository executePointRepository;
  private final ITaskRecordRepository taskRecordRepository;
  private final IFeatureHistoryRepository featureHistoryRepository;
  private final IExecuteTemplateRepository executeTemplateRepository;

  public FeatureExecuteProxy(RequestProxy requestProxy,
                             @Qualifier("featureExecutorPool") Executor executorService, TaskEndProcessor taskEndProcessor,
                             IExecutePointRepository executePointRepository, ITaskRecordRepository taskRecordRepository,
                             IFeatureHistoryRepository featureHistoryRepository, IExecuteTemplateRepository executeTemplateRepository) {
    this.requestProxy = requestProxy;
    this.executorService = executorService;
    this.taskEndProcessor = taskEndProcessor;
    this.executePointRepository = executePointRepository;
    this.taskRecordRepository = taskRecordRepository;
    this.featureHistoryRepository = featureHistoryRepository;
    this.executeTemplateRepository = executeTemplateRepository;
  }

  public void execute(FeatureTask featureTask) {
    featureTaskMap.put(featureTask.getTaskRecordId(), featureTask);
    CompletableFuture.supplyAsync(() -> {
      LinkedBlockingQueue<String> featureIds = featureTask.getFeatureIds();
      String featureId = featureIds.poll();
      String taskRecordId = featureTask.getTaskRecordId();
      if (StringUtils.isBlank(featureId)) {
        featureTaskMap.remove(taskRecordId);
        return null;
      }

      TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
      if (!taskRecordId.startsWith(FeatureDispatch.TEMP_KEY) && (Objects.isNull(taskRecord)
          || ProcessStatus.isCompleteStatus(taskRecord.getStatus()))) {
        log.info("task record is done not execute status={}", taskRecord.getStatus());
        return null;
      }

      FeatureExecuteParam featureExecuteParam = getFeatureExecuteParam(featureTask, featureId);
      featureExecuteParam.setDispatchType(DispatchType.FEATURE.name());
      featureExecuteParam.setMasterIp(IpUtils.getLocalIP());
      requestProxy.sendDispatchTask(featureExecuteParam, false, null);
      return featureId;
    }, executorService).whenComplete((featureId, e) -> {
      String recordId = Optional.ofNullable(featureId).orElse(TASK_FEATURE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally(e -> {
      log.error("handle task error", e);
      return null;
    });
  }

  public boolean isExitTask(String recordId) {
    if (StringUtils.isBlank(recordId)) {
      return false;
    }
    return featureTaskMap.containsKey(recordId);
  }

  private FeatureExecuteParam getFeatureExecuteParam(FeatureTask featureTask, String featureId) {
    FeatureExecuteParam featureExecuteParam = new FeatureExecuteParam();
    featureExecuteParam.setFeatureId(featureId);
    featureExecuteParam.setExecuteContext(featureTask.getExecuteContext().toMap());
    featureExecuteParam.setTaskRecordId(featureTask.getTaskRecordId());
    List<ExecutePointDto> executePoints = executePointRepository.getExecutePointByFeatureId(featureId);
    //将用例关联模版信息也添加到用例信息中
    executePoints.forEach(executePoint -> {
      ExecutorUnit executorUnit = wrapExecutorUnitTemplate(executePoint);
      executePoint.setFeatureInfo(JSON.toJSONString(executorUnit));
    });
    featureExecuteParam.setExecutePointList(executePoints);
    return featureExecuteParam;
  }

  private ExecutorUnit wrapExecutorUnitTemplate(ExecutePointDto executePoint) {
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
    if (StringUtils.isBlank(executorUnit.getRelatedId())) {
      return executorUnit;
    }
    ExecuteTemplateDto executeTemplate = executeTemplateRepository.getExecuteTemplate(executorUnit.getRelatedId());
    if (Objects.isNull(executeTemplate)) {
      return executorUnit;
    }
    ExecutorUnit related = OrikaUtil.convert(executeTemplate, ExecutorUnit.class);
    related.setParams(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
    related.setHeaders((Map<String, String>)JSON.parse(executeTemplate.getHeader()));
    executorUnit.setRelatedTemplate(related);
    return executorUnit;
  }

  public void featureStatusChange(String taskRecordId, FeatureHistoryDto history, Map<String, Object> context) {
    //每个用例执行完成之后都需要判断下是整个任务是否执行完成
    FeatureTask featureTask = featureTaskMap.get(taskRecordId);
    if (Objects.isNull(featureTask)) {
      log.info("can not find task record");
      return;
    }

    ProcessStatus processStatus = ProcessStatus.exchange(history.getExecuteStatus());
    boolean isTaskEnd = taskEndProcessor.process(taskRecordId, processStatus,
        featureTask.getLogId());
    if (isTaskEnd) {
      featureTaskMap.remove(taskRecordId);
      return;
    }

    if (MapUtils.isNotEmpty(context)) {
      ExecuteContext executeContext = featureTask.getExecuteContext();
      executeContext.toMap().putAll(context);
      featureTask.setExecuteContext(executeContext);
    }

    log.info("feature task start cycle run");
    execute(featureTask);
  }

  @Override
  @Subscribe
  @AllowConcurrentEvents
  public void stopEvent(InnerEvent event) {
    if (!Objects.equals(event.getLogType().getType(), LogType.FEATURE_TASK.getType())) {
      return;
    }

    String taskRecordId = event.getTargetId();
    FeatureTask featureTask = featureTaskMap.remove(taskRecordId);
    if (Objects.nonNull(featureTask)) {
      featureTaskMap.remove(taskRecordId);
    }

    log.info("stop pipeline task taskId={} taskRecordId={}", featureTask.getTaskId(),
        featureTask.getTaskRecordId());
    taskRecordRepository.updateRecordStatus(taskRecordId, ProcessStatus.STOP.getType());
    featureHistoryRepository.stopTaskFeatures(taskRecordId, ProcessStatus.STOP);
  }

  public Integer getTaskSize() {
    return featureTaskMap.values().stream().mapToInt(task -> task.getFeatureIds().size()).sum();
  }
}
