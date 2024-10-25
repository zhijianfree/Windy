package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.FeatureStatus;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.feature.entity.HistoryNodeDto;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TaskRecordService {

  private final FeatureHistoryService featureHistoryService;
  private final FeatureService featureService;
  private final ITaskRecordRepository taskRecordRepository;

  public TaskRecordService(FeatureHistoryService featureHistoryService,
      FeatureService featureService, ITaskRecordRepository taskRecordRepository) {
    this.featureHistoryService = featureHistoryService;
    this.featureService = featureService;
    this.taskRecordRepository = taskRecordRepository;
  }

  public boolean insert(TaskRecordDto taskRecordDto) {
    return taskRecordRepository.save(taskRecordDto);
  }

  public boolean updateRecordStatus(String recordId, int status) {
    return taskRecordRepository.updateRecordStatus(recordId, status);
  }

  public PageSize<TaskRecordDto> getTaskRecordPage(Integer pageNum, Integer size) {
    IPage<TaskRecordDto> recordIPage = taskRecordRepository.getTaskRecordPage(pageNum, size);
    PageSize<TaskRecordDto> pageSize = new PageSize<>();
    pageSize.setTotal(recordIPage.getTotal());
    pageSize.setData(recordIPage.getRecords());
    return pageSize;
  }

  public boolean deleteTaskRecord(String recordId) {
    TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(recordId);
    boolean executeHistory = featureHistoryService.deleteByRecordId(taskRecord.getTestCaseId());
    boolean flag = taskRecordRepository.deleteTaskRecord(recordId);
    return executeHistory && flag;
  }

  public List<FeatureHistoryDto>  getTaskFeatureHistories(String recordId) {
    return  featureHistoryService.getHistories(recordId);
  }

  public List<HistoryNodeDto> getTaskFeatureHistoryTree(String recordId) {
    List<FeatureHistoryDto> histories = featureHistoryService.getHistories(recordId);
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    Map<String, FeatureHistoryDto> historyMap = histories.stream()
        .collect(Collectors.toMap(FeatureHistoryDto::getFeatureId, history -> history));
    TaskRecordDto taskRecord = getTaskRecord(recordId);
    String testCaseId = taskRecord.getTestCaseId();
    List<FeatureInfoDto> featureInfos = featureService.queryFeatureList(testCaseId);
    List<HistoryNodeDto> historyNodes = featureInfos.stream().map(feature -> {
      HistoryNodeDto historyNodeDto = new HistoryNodeDto();
      historyNodeDto.setParentId(feature.getParentId());
      historyNodeDto.setRecordId(recordId);
      historyNodeDto.setSkip(Objects.equals(feature.getStatus(), FeatureStatus.DISABLE.getType()));
      historyNodeDto.setFeatureId(feature.getFeatureId());
      historyNodeDto.setFeatureName(feature.getFeatureName());
      FeatureHistoryDto featureHistory = historyMap.get(feature.getFeatureId());
      if (Objects.nonNull(featureHistory)) {
        historyNodeDto.setHistoryId(featureHistory.getHistoryId());
        historyNodeDto.setExecuteStatus(featureHistory.getExecuteStatus());
      }
      return historyNodeDto;
    }).collect(Collectors.toList());

    HistoryNodeDto root = new HistoryNodeDto();
    convertTree(historyNodes, root);
    return root.getChildren();
  }

  private void convertTree(List<HistoryNodeDto> featureList, HistoryNodeDto parent) {
    if (CollectionUtils.isEmpty(featureList)) {
      return;
    }

    List<HistoryNodeDto> list = featureList.stream()
        .filter(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()))
        .collect(Collectors.toList());
    parent.setChildren(list);

    featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
    list.forEach(node -> convertTree(featureList, node));
  }

  public TaskRecordDto getTaskRecord(String recordId) {
    return taskRecordRepository.getTaskRecord(recordId);
  }

  public List<TaskRecordDto> getTaskRecordsByTaskId(String taskId) {
    return taskRecordRepository.getTaskRecordsOrderByTime(taskId);
  }

  public Boolean stopTaskRecord(String recordId) {
    //todo notify master to stop dispatch task
    return taskRecordRepository.deleteTaskRecord(recordId);
  }

  public TaskRecordDto getTaskRecordByTrigger(String triggerId) {
    return taskRecordRepository.getTaskRecordByTrigger(triggerId);
  }

  public List<TaskRecordDto> getTriggerTaskRecords(String triggerId) {
    return taskRecordRepository.getTriggerTaskRecords(triggerId);
  }
}
