package com.zj.master.dispatch.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.dto.log.SubTaskLogDto;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.feature.ITaskRepository;
import com.zj.domain.repository.log.ISubTaskLogRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.common.enums.LogType;
import com.zj.master.entity.vo.ExecuteContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class TaskDispatch implements IDispatchExecutor {

  @Autowired
  private ITaskRepository taskRepository;

  @Autowired
  private IFeatureRepository featureRepository;

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

  @Autowired
  private FeatureExecuteProxy featureExecuteProxy;

  @Autowired
  private ISubTaskLogRepository subTaskLogRepository;

  @Autowired
  private IDispatchLogRepository dispatchLogRepository;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Override
  public Integer type() {
    return LogType.FEATURE_TASK.getType();
  }

  @Override
  public boolean dispatch(TaskDetailDto task) {
    TaskInfoDto taskDetail = taskRepository.getTaskDetail(task.getSourceId());
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", task.getSourceId());
      return false;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfoDto> featureList = featureRepository.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return false;
    }

    TaskRecordDto taskRecordDto = buildTaskRecordDTO(taskDetail);
    taskRecordRepository.save(taskRecordDto);

    dispatchLogRepository.updateLogSourceRecord(task.getTaskLogId(), taskRecordDto.getRecordId());

    FeatureTask featureTask = buildFeatureTask(task, featureList, taskRecordDto);
    saveSubTaskLog(featureTask);
    featureExecuteProxy.execute(featureTask);
    return false;
  }

  private void saveSubTaskLog(FeatureTask featureTask) {
    List<SubTaskLogDto> subTaskLogs = featureTask.getFeatureIds().stream()
        .map(featureId -> buildSubTaskLog(featureTask, featureId)).collect(Collectors.toList());
    subTaskLogRepository.batchSaveLogs(subTaskLogs);
  }

  private SubTaskLogDto buildSubTaskLog(FeatureTask featureTask, String featureId) {
    SubTaskLogDto subTaskLogDto = new SubTaskLogDto();
    subTaskLogDto.setSubTaskId(uniqueIdService.getUniqueId());
    subTaskLogDto.setLogId(featureTask.getLogId());
    subTaskLogDto.setExecuteId(featureId);
    subTaskLogDto.setStatus(ProcessStatus.RUNNING.getType());
    long dateNow = System.currentTimeMillis();
    subTaskLogDto.setCreateTime(dateNow);
    subTaskLogDto.setUpdateTime(dateNow);
    return subTaskLogDto;
  }

  private FeatureTask buildFeatureTask(TaskDetailDto task, List<FeatureInfoDto> featureList,
      TaskRecordDto taskRecordDto) {
    FeatureTask featureTask = new FeatureTask();
    ExecuteContext executeContext = buildTaskConfig(taskRecordDto.getTaskConfig());
    featureTask.setExecuteContext(executeContext);

    List<String> featureIds = featureList.stream().map(FeatureInfoDto::getFeatureId)
        .collect(Collectors.toList());
    featureTask.addAll(featureIds);

    featureTask.setTaskRecordId(taskRecordDto.getRecordId());
    featureTask.setTaskId(task.getSourceId());
    featureTask.setLogId(task.getTaskLogId());
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

  private TaskRecordDto buildTaskRecordDTO(TaskInfoDto taskDetail) {
    TaskRecordDto taskRecordDTO = new TaskRecordDto();
    taskRecordDTO.setTaskConfig(taskDetail.getTaskConfig());
    taskRecordDTO.setTaskName(taskDetail.getTaskName());
    taskRecordDTO.setTaskId(taskDetail.getTaskId());
    taskRecordDTO.setRecordId(uniqueIdService.getUniqueId());
    taskRecordDTO.setUserId("admin");
    taskRecordDTO.setStatus(ProcessStatus.RUNNING.getType());
    taskRecordDTO.setMachines(taskDetail.getMachines());
    taskRecordDTO.setTestCaseId(taskDetail.getTestCaseId());
    taskRecordDTO.setCreateTime(System.currentTimeMillis());
    taskRecordDTO.setUpdateTime(System.currentTimeMillis());
    return taskRecordDTO;
  }

  @Override
  public boolean resume(DispatchLogDto taskLog) {
    TaskInfoDto taskDetail = taskRepository.getTaskDetail(taskLog.getSourceId());
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", taskLog.getSourceId());
      return false;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfoDto> featureList = featureRepository.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return false;
    }
    List<SubTaskLogDto> tasks = subTaskLogRepository.getSubTaskByLogId(taskLog.getLogId());
    List<String> completedFeatures = tasks.stream()
        .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
        .map(SubTaskLogDto::getExecuteId)
        .collect(Collectors.toList());

    FeatureTask featureTask = buildResumeFeatureTask(taskLog, taskDetail,
        featureList, completedFeatures);
    featureExecuteProxy.execute(featureTask);
    return true;
  }

  private FeatureTask buildResumeFeatureTask(DispatchLogDto taskLog, TaskInfoDto taskDetail,
      List<FeatureInfoDto> featureList, List<String> completedFeatures) {
    FeatureTask featureTask = new FeatureTask();
    ExecuteContext executeContext = buildTaskConfig(taskDetail.getTaskConfig());
    featureTask.setExecuteContext(executeContext);

    //已执行完成状态的任务无需在恢复
    List<String> featureIds = featureList.stream()
        .map(FeatureInfoDto::getFeatureId)
        .filter(featureId -> !completedFeatures.contains(featureId))
        .collect(Collectors.toList());
    featureTask.addAll(featureIds);

    featureTask.setTaskRecordId(taskLog.getSourceRecordId());
    featureTask.setTaskId(taskLog.getSourceId());
    featureTask.setLogId(taskLog.getLogId());
    return featureTask;
  }
}
