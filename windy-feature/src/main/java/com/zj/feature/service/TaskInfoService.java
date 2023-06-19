package com.zj.feature.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchModel;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseStatusModel;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.po.feature.TaskInfo;
import com.zj.domain.repository.feature.ITaskRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2022/12/29
 */
@Slf4j
@Service
public class TaskInfoService {

  @Autowired
  private TaskRecordService taskRecordService;

  @Autowired
  private FeatureHistoryService featureHistoryService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private ITaskRepository taskRepository;

  @Autowired
  private RequestProxy requestProxy;

  public static final String FORMAT_TIPS = "任务执行状态: 成功数: %s 成功率百分比: %s";

  public PageSize<TaskInfoDto> getTaskList(String name, Integer pageNum, Integer size) {
    IPage<TaskInfo> taskInfoIPage = taskRepository.getTaskList(name, pageNum, size);
    PageSize<TaskInfoDto> pageSize = new PageSize<>();
    pageSize.setTotal(taskInfoIPage.getTotal());
    if (CollectionUtils.isEmpty(taskInfoIPage.getRecords())) {
      pageSize.setData(Collections.emptyList());
      return pageSize;
    }

    List<TaskInfoDto> dtoList = taskInfoIPage.getRecords().stream().map(task -> {
      TaskInfoDto taskInfoDTO = OrikaUtil.convert(task, TaskInfoDto.class);
      boolean isRunning = isTaskRunning(taskInfoDTO);
      taskInfoDTO.setIsRunning(isRunning);
      return taskInfoDTO;
    }).collect(Collectors.toList());
    pageSize.setData(dtoList);
    return pageSize;
  }

  private boolean isTaskRunning(TaskInfoDto taskInfoDTO) {
    List<TaskRecordDto> taskRecords = taskRecordService.getTaskRecordsByTaskId(
        taskInfoDTO.getTaskId());
    if (CollectionUtils.isEmpty(taskRecords)) {
      return false;
    }

    return taskRecords.stream()
        .anyMatch(record -> Objects.equals(ProcessStatus.RUNNING.getType(), record.getStatus()));
  }

  public Boolean createTask(TaskInfoDto taskInfoDTO) {
    String taskId = uniqueIdService.getUniqueId();
    taskInfoDTO.setTaskId(taskId);
    return taskRepository.createTask(taskInfoDTO);
  }

  public Boolean updateTask(TaskInfoDto taskInfoDTO) {
    return taskRepository.updateTask(taskInfoDTO);
  }

  public Boolean deleteTask(String taskId) {
    return taskRepository.deleteTask(taskId);
  }

  public TaskInfoDto getTaskDetail(String taskId) {
    return taskRepository.getTaskDetail(taskId);
  }

  public Boolean startTask(String taskId) {
    TaskInfoDto taskDetail = getTaskDetail(taskId);
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", taskId);
      return false;
    }

    DispatchModel dispatchModel = new DispatchModel();
    dispatchModel.setType(LogType.FEATURE_TASK.getType());
    dispatchModel.setSourceId(taskId);
    dispatchModel.setSourceName(taskDetail.getTaskName());
    return requestProxy.runTask(dispatchModel);
  }

  public ResponseStatusModel getTaskStatus(String taskId) {
    List<TaskRecordDto> taskRecords = taskRecordService.getTaskRecordsByTaskId(taskId);
    TaskRecordDto taskRecord = taskRecords.get(0);
    Integer status = taskRecord.getStatus();
    ResponseStatusModel responseStatusModel = new ResponseStatusModel();
    responseStatusModel.setStatus(status);

    List<FeatureHistoryDto> histories = featureHistoryService.getHistories(
        taskRecord.getRecordId());
    long successCount = histories.stream().filter(
            history -> Objects.equals(history.getExecuteStatus(), ProcessStatus.SUCCESS.getType()))
        .count();

    JSONObject jsonObject = new JSONObject();
    Float percent = (successCount * 1F / histories.size()) * 100;
    jsonObject.put("percent", percent.intValue());
    responseStatusModel.setData(jsonObject);
    String msg = String.format(FORMAT_TIPS, successCount, percent);
    responseStatusModel.setMessage(msg);
    return responseStatusModel;
  }

  public List<TaskInfoDto> getAllTaskList(String serviceId) {
    return taskRepository.getAllTaskList(serviceId);
  }
}
