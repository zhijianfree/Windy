package com.zj.master.dispatch.task;

import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.master.dispatch.ClientProxy;
import com.zj.master.dispatch.feature.FeatureDispatch;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class FeatureExecuteProxy implements IStopEventListener {

  public static final String DISPATCH_FEATURE_TYPE = "FEATURE";
  @Autowired
  private ClientProxy clientProxy;
  @Autowired
  @Qualifier("featureExecutorPool")
  private ExecutorService executorService;

  @Autowired
  private TaskEndProcessor taskEndProcessor;

  @Autowired
  private IExecutePointRepository executePointRepository;

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  public static final String TASK_FEATURE_TIPS = "no task need run";
  private final Map<String, FeatureTask> featureTaskMap = new ConcurrentHashMap<>();

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
      featureExecuteParam.setDispatchType(DISPATCH_FEATURE_TYPE);
      featureExecuteParam.setMasterIp(IpUtils.getLocalIP());
      clientProxy.sendDispatchTask(featureExecuteParam);
      return featureId;
    }, executorService).whenComplete((featureId, e) -> {
      String recordId = Optional.ofNullable(featureId).orElse(TASK_FEATURE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally((e) -> {
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
    List<ExecutePointDto> executePoints = executePointRepository.getExecutePointByFeatureId(
        featureId);
    featureExecuteParam.setExecutePointList(executePoints);
    return featureExecuteParam;
  }

  public void featureStatusChange(String taskRecordId, FeatureHistoryDto history) {
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

    log.info("feature task start cycle run");
    execute(featureTask);
  }

  @Override
  @Subscribe
  public void handle(InnerEvent event) {
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
}
