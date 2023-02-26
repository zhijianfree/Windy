package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.dto.TaskInfoDTO;
import com.zj.feature.entity.dto.TaskRecordDTO;
import com.zj.feature.entity.po.FeatureInfo;
import com.zj.feature.entity.po.TaskInfo;
import com.zj.feature.entity.po.TaskRecord;
import com.zj.feature.entity.type.ExecuteStatusEnum;
import com.zj.feature.entity.vo.FeatureConstant;
import com.zj.feature.executor.IFeatureExecutor;
import com.zj.feature.mapper.TaskInfoMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2022/12/29
 */
@Slf4j
@Service
public class TaskInfoService extends ServiceImpl<TaskInfoMapper, TaskInfo> {

  @Autowired
  private FeatureService featureService;

  @Autowired
  private IFeatureExecutor IFeatureExecutor;

  @Autowired
  private TaskRecordService taskRecordService;

  @Autowired
  private ICacheService cacheService;

  public PageSize<TaskInfoDTO> getTaskList(String name, Integer pageNum, Integer size) {
    LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery(TaskInfo.class);
    if (StringUtils.isNoneBlank(name)) {
      wrapper = wrapper.like(TaskInfo::getTaskName, name);
    }
    IPage<TaskInfo> page = new Page<>(pageNum, size);
    IPage<TaskInfo> taskInfoIPage = page(page, wrapper);

    PageSize<TaskInfoDTO> pageSize = new PageSize<>();
    pageSize.setTotal(taskInfoIPage.getTotal());
    if (CollectionUtils.isEmpty(taskInfoIPage.getRecords())) {
      pageSize.setData(Collections.emptyList());
      return pageSize;
    }

    List<TaskInfoDTO> dtoList = taskInfoIPage.getRecords().stream().map(task -> {
      TaskInfoDTO taskInfoDTO = new TaskInfoDTO();
      BeanUtils.copyProperties(task, taskInfoDTO);

      boolean isRunning = isTaskRunning(taskInfoDTO);
      taskInfoDTO.setIsRunning(isRunning);

      return taskInfoDTO;
    }).collect(Collectors.toList());
    pageSize.setData(dtoList);
    return pageSize;
  }

  private boolean isTaskRunning(TaskInfoDTO taskInfoDTO) {
    List<TaskRecord> taskRecords = taskRecordService.list(Wrappers.lambdaQuery(TaskRecord.class)
        .eq(TaskRecord::getTaskId, taskInfoDTO.getTaskId()));
    if (CollectionUtils.isEmpty(taskRecords)) {
      return false;
    }

    return taskRecords.stream().anyMatch(record -> {
      String cache = cacheService
          .getCache(FeatureConstant.RECORD_STATUS_CACHE_KEY + record.getRecordId());
      return StringUtils.isNoneBlank(cache) || Objects
          .equals(record.getStatus(), ExecuteStatusEnum.RUNNING.getStatus());
    });
  }

  public Boolean createTask(TaskInfoDTO taskInfoDTO) {
    TaskInfo taskInfo = new TaskInfo();
    BeanUtils.copyProperties(taskInfoDTO, taskInfo);
    taskInfo.setTaskId(UUID.randomUUID().toString().replace("-", ""));
    taskInfo.setCreateTime(System.currentTimeMillis());
    taskInfo.setUpdateTime(System.currentTimeMillis());
    return save(taskInfo);
  }

  public Boolean updateTask(TaskInfoDTO taskInfoDTO) {
    TaskInfo taskInfo = new TaskInfo();
    BeanUtils.copyProperties(taskInfoDTO, taskInfo);
    taskInfo.setUpdateTime(System.currentTimeMillis());

    return update(taskInfo,
        Wrappers.lambdaUpdate(TaskInfo.class).eq(TaskInfo::getTaskId, taskInfoDTO.getTaskId()));
  }

  public Boolean deleteTask(String taskId) {
    return remove(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
  }

  public TaskInfoDTO getTaskDetail(String taskId) {
    TaskInfo taskInfo = getOne(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));

    TaskInfoDTO taskInfoDTO = new TaskInfoDTO();
    BeanUtils.copyProperties(taskInfo, taskInfoDTO);
    return taskInfoDTO;
  }

  public String startTask(String taskId) {
    TaskInfoDTO taskDetail = getTaskDetail(taskId);
    if (Objects.isNull(taskDetail)) {
      log.info("can not find task={}", taskId);
      return null;
    }

    String testCaseId = taskDetail.getTestCaseId();
    List<FeatureInfo> featureList = featureService.queryNotContainFolder(testCaseId);
    if (CollectionUtils.isEmpty(featureList)) {
      log.info("can not find feature list by testCaseId={}", testCaseId);
      return null;
    }
    TaskRecordDTO taskRecordDTO = buildTaskRecordDTO(taskDetail);
    taskRecordService.insert(taskRecordDTO);

    saveCache(featureList, taskRecordDTO);

    List<String> featureIds = featureList.stream().map(FeatureInfo::getFeatureId)
        .collect(Collectors.toList());
    IFeatureExecutor.batchRunTask(featureIds, taskRecordDTO);
    return taskRecordDTO.getRecordId();
  }

  private void saveCache(List<FeatureInfo> featureList, TaskRecordDTO taskRecordDTO) {
    Map<String, Integer> map = featureList.stream()
        .collect(Collectors.toMap(FeatureInfo::getFeatureId,
            feature -> ExecuteStatusEnum.RUNNING.getStatus()));
    String recordId = FeatureConstant.RECORD_STATUS_CACHE_KEY + taskRecordDTO.getRecordId();
    cacheService.setCache(recordId, JSON.toJSONString(map));
  }

  private TaskRecordDTO buildTaskRecordDTO(TaskInfoDTO taskDetail) {
    TaskRecordDTO taskRecordDTO = new TaskRecordDTO();
    taskRecordDTO.setTaskConfig(taskDetail.getTaskConfig());
    taskRecordDTO.setTaskName(taskDetail.getTaskName());
    taskRecordDTO.setTaskId(taskDetail.getTaskId());
    taskRecordDTO.setRecordId(UUID.randomUUID().toString().replace("-", ""));
    taskRecordDTO.setUserId("admin");
    taskRecordDTO.setStatus(ExecuteStatusEnum.RUNNING.getStatus());
    taskRecordDTO.setMachines(taskDetail.getMachines());
    taskRecordDTO.setTestCaseId(taskDetail.getTestCaseId());
    taskRecordDTO.setCreateTime(System.currentTimeMillis());
    taskRecordDTO.setUpdateTime(System.currentTimeMillis());
    return taskRecordDTO;
  }
}
