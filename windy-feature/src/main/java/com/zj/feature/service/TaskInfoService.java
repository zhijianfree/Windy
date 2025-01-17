package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.common.entity.dto.ResponseStatusModel.PercentStatics;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.po.feature.TaskInfo;
import com.zj.domain.repository.feature.ITaskRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/29
 */
@Slf4j
@Service
public class TaskInfoService {

  private final TaskRecordService taskRecordService;
  private final FeatureHistoryService featureHistoryService;
  private final UniqueIdService uniqueIdService;
  private final ITaskRepository taskRepository;
  private final IMasterInvoker masterInvoker;
  private final IAuthService authService;
  public static final String FORMAT_TIPS = "任务执行状态: 总任务数:%s 成功数: %s 成功率百分比: %s %";

  public TaskInfoService(TaskRecordService taskRecordService,
                         FeatureHistoryService featureHistoryService, UniqueIdService uniqueIdService,
                         ITaskRepository taskRepository, IMasterInvoker masterInvoker, IAuthService authService) {
    this.taskRecordService = taskRecordService;
    this.featureHistoryService = featureHistoryService;
    this.uniqueIdService = uniqueIdService;
    this.taskRepository = taskRepository;
    this.masterInvoker = masterInvoker;
    this.authService = authService;
  }

  public PageSize<TaskInfoBO> getTaskList(String name, Integer pageNum, Integer size) {
    String currentUserId = authService.getCurrentUserId();
    IPage<TaskInfo> taskInfoIPage = taskRepository.getFuzzTaskList(name, pageNum, size, currentUserId);
    PageSize<TaskInfoBO> pageSize = new PageSize<>();
    pageSize.setTotal(taskInfoIPage.getTotal());
    if (CollectionUtils.isEmpty(taskInfoIPage.getRecords())) {
      pageSize.setData(Collections.emptyList());
      return pageSize;
    }

    List<TaskInfoBO> dtoList = taskInfoIPage.getRecords().stream().map(task -> {
      TaskInfoBO taskInfoBO = OrikaUtil.convert(task, TaskInfoBO.class);
      boolean isRunning = isTaskRunning(taskInfoBO);
      taskInfoBO.setIsRunning(isRunning);
      return taskInfoBO;
    }).collect(Collectors.toList());
    pageSize.setData(dtoList);
    return pageSize;
  }

  private boolean isTaskRunning(TaskInfoBO taskInfoBO) {
    List<TaskRecordBO> taskRecords = taskRecordService.getTaskRecordsByTaskId(
        taskInfoBO.getTaskId());
    if (CollectionUtils.isEmpty(taskRecords)) {
      return false;
    }

    return taskRecords.stream()
        .anyMatch(taskRecord -> Objects.equals(ProcessStatus.RUNNING.getType(), taskRecord.getStatus()));
  }

  public Boolean createTask(TaskInfoBO taskInfoBO) {
    String taskId = uniqueIdService.getUniqueId();
    taskInfoBO.setTaskId(taskId);
    taskInfoBO.setUserId(authService.getCurrentUserId());
    return taskRepository.createTask(taskInfoBO);
  }

  public Boolean updateTask(TaskInfoBO taskInfoBO) {
    return taskRepository.updateTask(taskInfoBO);
  }

  public Boolean deleteTask(String taskId) {
    return taskRepository.deleteTask(taskId);
  }

  public TaskInfoBO getTaskDetail(String taskId) {
    return taskRepository.getTaskDetail(taskId);
  }

  public Boolean startTask(String taskId) {
    TaskInfoBO taskDetail = getTaskDetail(taskId);
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", taskId);
      throw new ApiException(ErrorCode.TASK_NOT_FIND);
    }

    DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
    dispatchTaskModel.setType(LogType.FEATURE_TASK.getType());
    UserDetail userDetail = authService.getUserDetail();
    String user = Optional.ofNullable(userDetail.getNickName()).orElseGet(userDetail::getUserName);
    dispatchTaskModel.setUser(user);
    dispatchTaskModel.setSourceId(taskId);
    dispatchTaskModel.setSourceName(taskDetail.getTaskName());
    String recordId = masterInvoker.runFeatureTask(dispatchTaskModel);
    if (StringUtils.isBlank(recordId)) {
      log.info("run task failed taskId={}", taskId);
      throw new ApiException(ErrorCode.TASK_RUN_FAILED);
    }
    return true;
  }

  public ResponseStatusModel getTaskStatus(String taskId) {
    List<TaskRecordBO> taskRecords = taskRecordService.getTaskRecordsByTaskId(taskId);
    TaskRecordBO taskRecord = taskRecords.get(0);
    Integer status = taskRecord.getStatus();
    ResponseStatusModel responseStatusModel = new ResponseStatusModel();
    responseStatusModel.setStatus(status);

    List<FeatureHistoryBO> histories = featureHistoryService.getHistories(
        taskRecord.getRecordId());
    long successCount = histories.stream().filter(
            history -> Objects.equals(history.getExecuteStatus(), ProcessStatus.SUCCESS.getType()))
        .count();

    PercentStatics percentStatics = new PercentStatics();
    Float percent = (successCount * 1F / histories.size()) * 100;
    percentStatics.setPercent(percent.intValue());
    responseStatusModel.setData(percentStatics);

    String msg = String.format(FORMAT_TIPS, successCount, percent);
    responseStatusModel.setMessage(msg);
    return responseStatusModel;
  }

  public List<TaskInfoBO> getAllTaskList(String serviceId) {
    return taskRepository.getAllTaskList(serviceId);
  }
}
