package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseStatusModel;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.repository.feature.ITaskRepository;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.po.feature.TaskInfo;
import com.zj.domain.entity.po.feature.TaskRecord;
import com.zj.feature.entity.vo.FeatureConstant;
import com.zj.feature.executor.IFeatureExecutor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/29
 */
@Slf4j
@Service
public class TaskInfoService {

  public static final String FORMAT_TIPS = "任务执行状态: 成功数: %s 成功率百分比: %s";
  @Autowired
  private FeatureService featureService;

  @Autowired
  private IFeatureExecutor IFeatureExecutor;

  @Autowired
  private TaskRecordService taskRecordService;

  @Autowired
  private FeatureHistoryService featureHistoryService;

  @Autowired
  private ICacheService cacheService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private ITaskRepository taskRepository;

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
    List<TaskRecordDto> taskRecords = taskRecordService.getTaskRecordsByTaskId(taskInfoDTO.getTaskId());
    if (CollectionUtils.isEmpty(taskRecords)) {
      return false;
    }

    return taskRecords.stream().anyMatch(record -> {
      String cache = cacheService.getCache(
          FeatureConstant.RECORD_STATUS_CACHE_KEY + record.getRecordId());
      return StringUtils.isNoneBlank(cache) || Objects.equals(record.getStatus(),
          ProcessStatus.RUNNING.getType());
    });
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

  public String startTask(String taskId) {
    TaskInfoDto taskDetail = getTaskDetail(taskId);
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", taskId);
      return null;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfoDto> featureList = featureService.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return null;
    }
    TaskRecordDto taskRecordDTO = buildTaskRecordDTO(taskDetail);
    taskRecordService.insert(taskRecordDTO);

    saveCache(featureList, taskRecordDTO);

    List<String> featureIds = featureList.stream().map(FeatureInfoDto::getFeatureId)
        .collect(Collectors.toList());
    IFeatureExecutor.batchRunTask(featureIds, taskRecordDTO);
    return taskRecordDTO.getRecordId();
  }

  private void saveCache(List<FeatureInfoDto> featureList, TaskRecordDto taskRecordDTO) {
    Map<String, Integer> map = featureList.stream().collect(
        Collectors.toMap(FeatureInfoDto::getFeatureId,
            feature -> ProcessStatus.RUNNING.getType()));
    String recordId = FeatureConstant.RECORD_STATUS_CACHE_KEY + taskRecordDTO.getRecordId();
    cacheService.setCache(recordId, JSON.toJSONString(map));
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
    Float percent = (successCount * 1F/ histories.size()) * 100;
    jsonObject.put("percent", percent.intValue());
    responseStatusModel.setData(jsonObject);
    String msg = String.format(FORMAT_TIPS, successCount,percent);
    responseStatusModel.setMessage(msg);
    return responseStatusModel;
  }

  public List<TaskInfoDto> getAllTaskList(String serviceId) {
    return taskRepository.getAllTaskList(serviceId);
  }
}
