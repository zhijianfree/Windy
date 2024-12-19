package com.zj.master.dispatch.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.FeatureStatus;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.feature.ITaskRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.entity.vo.ExecuteContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class TaskDispatch implements IDispatchExecutor {

  private final ITaskRepository taskRepository;
  private final IFeatureRepository featureRepository;
  private final ITaskRecordRepository taskRecordRepository;
  private final FeatureExecuteProxy featureExecuteProxy;
  private final ISubDispatchLogRepository subTaskLogRepository;
  private final IDispatchLogRepository dispatchLogRepository;
  private final UniqueIdService uniqueIdService;

  public TaskDispatch(ITaskRepository taskRepository, IFeatureRepository featureRepository,
      ITaskRecordRepository taskRecordRepository, FeatureExecuteProxy featureExecuteProxy,
      ISubDispatchLogRepository subTaskLogRepository, IDispatchLogRepository dispatchLogRepository,
      UniqueIdService uniqueIdService) {
    this.taskRepository = taskRepository;
    this.featureRepository = featureRepository;
    this.taskRecordRepository = taskRecordRepository;
    this.featureExecuteProxy = featureExecuteProxy;
    this.subTaskLogRepository = subTaskLogRepository;
    this.dispatchLogRepository = dispatchLogRepository;
    this.uniqueIdService = uniqueIdService;
  }

  @Override
  public LogType type() {
    return LogType.FEATURE_TASK;
  }

  @Override
  public boolean isExistInJvm(DispatchLogBO taskLog) {
    return featureExecuteProxy.isExitTask(taskLog.getSourceRecordId());
  }

  @Override
  public String dispatch(DispatchTaskModel task, String logId) {
    TaskInfoBO taskDetail = taskRepository.getTaskDetail(task.getSourceId());
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", task.getSourceId());
      return null;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfoBO> featureList = featureRepository.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return "";
    }

    TaskRecordBO taskRecordBO = buildTaskRecordDTO(taskDetail, task.getTriggerId());
    taskRecordRepository.save(taskRecordBO);

    dispatchLogRepository.updateLogSourceRecord(logId, taskRecordBO.getRecordId());

    List<String> featureIds =
            featureList.stream().filter(feature -> Objects.equals(feature.getStatus(), FeatureStatus.NORMAL.getType()))
                    .map(FeatureInfoBO::getFeatureId).collect(Collectors.toList());
    FeatureTask featureTask = buildFeatureTask(task, logId, featureIds, taskRecordBO);
    saveSubTaskLog(featureIds, featureTask.getLogId());
    featureExecuteProxy.execute(featureTask);
    return taskRecordBO.getRecordId();
  }

  private List<SubDispatchLogBO> saveSubTaskLog(List<String> featureIds, String logId) {
    List<SubDispatchLogBO> subTaskLogs = featureIds.stream()
        .map(featureId -> buildSubTaskLog(logId, featureId)).collect(Collectors.toList());
    subTaskLogRepository.batchSaveLogs(subTaskLogs);
    return subTaskLogs;
  }

  private SubDispatchLogBO buildSubTaskLog(String logId, String featureId) {
    SubDispatchLogBO subDispatchLogBO = new SubDispatchLogBO();
    subDispatchLogBO.setSubTaskId(uniqueIdService.getUniqueId());
    subDispatchLogBO.setLogId(logId);
    subDispatchLogBO.setExecuteId(featureId);
    subDispatchLogBO.setStatus(ProcessStatus.RUNNING.getType());
    long dateNow = System.currentTimeMillis();
    subDispatchLogBO.setCreateTime(dateNow);
    subDispatchLogBO.setUpdateTime(dateNow);
    return subDispatchLogBO;
  }

  private FeatureTask buildFeatureTask(DispatchTaskModel task, String logId, List<String> featureIds,
      TaskRecordBO taskRecordBO) {
    FeatureTask featureTask = new FeatureTask();
    ExecuteContext executeContext = buildTaskConfig(taskRecordBO.getTaskConfig());
    featureTask.setExecuteContext(executeContext);
    featureTask.addAll(featureIds);

    featureTask.setTaskRecordId(taskRecordBO.getRecordId());
    featureTask.setTaskId(task.getSourceId());
    featureTask.setLogId(logId);
    return featureTask;
  }

  private ExecuteContext buildTaskConfig(String taskConfig) {
    ExecuteContext executeContext = new ExecuteContext();
    if (StringUtils.isBlank(taskConfig)) {
      return executeContext;
    }

    JSONObject jsonObject = JSON.parseObject(taskConfig);
    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
      executeContext.set(entry.getKey(), entry.getValue());
    }
    return executeContext;
  }

  private TaskRecordBO buildTaskRecordDTO(TaskInfoBO taskDetail, String triggerId) {
    TaskRecordBO taskRecordBO = new TaskRecordBO();
    taskRecordBO.setTaskConfig(taskDetail.getTaskConfig());
    taskRecordBO.setTaskName(taskDetail.getTaskName());
    taskRecordBO.setTaskId(taskDetail.getTaskId());
    taskRecordBO.setRecordId(uniqueIdService.getUniqueId());
    taskRecordBO.setTriggerId(triggerId);
    taskRecordBO.setStatus(ProcessStatus.RUNNING.getType());
    taskRecordBO.setMachines(taskDetail.getMachines());
    taskRecordBO.setTestCaseId(taskDetail.getTestCaseId());
    taskRecordBO.setCreateTime(System.currentTimeMillis());
    taskRecordBO.setUpdateTime(System.currentTimeMillis());
    return taskRecordBO;
  }

  @Override
  public boolean resume(DispatchLogBO dispatchLog) {
    TaskInfoBO taskDetail = taskRepository.getTaskDetail(dispatchLog.getSourceId());
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", dispatchLog.getSourceId());
      return false;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfoBO> featureList = featureRepository.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return false;
    }

    List<SubDispatchLogBO> subLogs = subTaskLogRepository.getSubLogByLogId(dispatchLog.getLogId());
    if (CollectionUtils.isEmpty(subLogs)) {
      //如果找不到子任务执行记录，那么就需要重新创建
      List<String> featureIds = featureList.stream().map(FeatureInfoBO::getFeatureId)
          .collect(Collectors.toList());
      subLogs = saveSubTaskLog(featureIds, dispatchLog.getLogId());
    }

    List<String> completedFeatures = subLogs.stream()
        .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
        .map(SubDispatchLogBO::getExecuteId)
        .collect(Collectors.toList());

    FeatureTask featureTask = buildResumeFeatureTask(dispatchLog, taskDetail,
        featureList, completedFeatures);
    if (StringUtils.isBlank(dispatchLog.getSourceRecordId())) {
      TaskRecordBO taskRecordBO = buildTaskRecordDTO(taskDetail, null);
      taskRecordRepository.save(taskRecordBO);
      featureTask.setTaskRecordId(taskRecordBO.getRecordId());
    }
    featureExecuteProxy.execute(featureTask);
    return true;
  }

  private FeatureTask buildResumeFeatureTask(DispatchLogBO taskLog, TaskInfoBO taskDetail,
                                             List<FeatureInfoBO> featureList, List<String> completedFeatures) {
    FeatureTask featureTask = new FeatureTask();
    ExecuteContext executeContext = buildTaskConfig(taskDetail.getTaskConfig());
    featureTask.setExecuteContext(executeContext);

    //已执行完成状态的任务无需在恢复
    List<String> featureIds = featureList.stream()
        .map(FeatureInfoBO::getFeatureId)
        .filter(featureId -> !completedFeatures.contains(featureId))
        .collect(Collectors.toList());
    featureTask.addAll(featureIds);

    featureTask.setTaskRecordId(taskLog.getSourceRecordId());
    featureTask.setTaskId(taskLog.getSourceId());
    featureTask.setLogId(taskLog.getLogId());
    return featureTask;
  }

  @Override
  public Integer getExecuteCount() {
    return 0;
  }
}
