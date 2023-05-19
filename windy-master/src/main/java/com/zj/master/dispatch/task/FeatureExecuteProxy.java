package com.zj.master.dispatch.task;

import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.master.dispatch.ClientProxy;
import com.zj.master.dispatch.listener.IInnerEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.common.model.StopDispatch;
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
public class FeatureExecuteProxy implements IInnerEventListener {

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

  public void execute(FeatureTask featureTask){
    CompletableFuture.supplyAsync(() -> {
      LinkedBlockingQueue<String> featureIds = featureTask.getFeatureIds();
      String featureId = featureIds.poll();
      if (StringUtils.isBlank(featureId)) {
        featureTaskMap.remove(featureTask.getTaskRecordId());
        return null;
      }

      FeatureExecuteParam featureExecuteParam = getFeatureExecuteParam(featureTask, featureId);
      featureExecuteParam.setDispatchType(DISPATCH_FEATURE_TYPE);
      featureExecuteParam.setMasterIp(IpUtils.getLocalIP());
      clientProxy.sendDispatchTask(featureExecuteParam);
      featureTaskMap.put(featureTask.getTaskRecordId(), featureTask);
      return featureId;
    }, executorService).whenComplete((featureId, e) -> {
      String recordId = Optional.ofNullable(featureId).orElse(TASK_FEATURE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally((e) ->{
      log.error("handle task error", e);
      return null;
    });
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
    ProcessStatus processStatus = ProcessStatus.exchange(history.getExecuteStatus());
    if (processStatus.isFailStatus()){
      featureHistoryRepository.updateStatus(history.getHistoryId(), processStatus.getType());
    }

    //每个用例执行完成之后都需要判断下是整个任务是否执行完成
    FeatureTask featureTask = featureTaskMap.get(taskRecordId);
    boolean isTaskEnd = taskEndProcessor.process(taskRecordId, processStatus, featureTask.getLogId());
    if (isTaskEnd){
      featureTaskMap.remove(taskRecordId);
      return;
    }

    if (Objects.nonNull(featureTask)) {
      log.info("feature task start cycle run");
      execute(featureTask);
    }
  }

  @Override
  @Subscribe
  public void handle(InnerEvent event) {
    if (!Objects.equals(event.getLogType().getType(), LogType.FEATURE_TASK.getType())) {
      return;
    }

    FeatureTask featureTask = featureTaskMap.remove(event.getTargetId());
    if (Objects.isNull(featureTask)) {
      log.info("remove feature task but not find it ={}", event.getTargetId());
      return;
    }


    log.info("stop pipeline task taskId={} taskRecordId={}", featureTask.getTaskId(),
        featureTask.getTaskRecordId());
    taskRecordRepository.updateRecordStatus(event.getTargetId(), ProcessStatus.STOP.getType());
  }
}
