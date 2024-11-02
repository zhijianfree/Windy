package com.zj.feature.service;

import com.zj.common.enums.FeatureStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.feature.entity.HistoryNodeDto;
import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Slf4j
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

  public PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size) {
    return taskRecordRepository.getTaskRecordPage(pageNum, size);
  }

  public boolean deleteTaskRecord(String recordId) {
    TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(recordId);
    if (Objects.isNull(taskRecord)) {
      return true;
    }
    List<FeatureHistoryBO> histories = featureHistoryService.getHistories(recordId);
    if (CollectionUtils.isNotEmpty(histories)) {
      boolean executeHistory = featureHistoryService.deleteByRecordId(recordId);
      log.info("delete record history result = {}", executeHistory);
    }
    return taskRecordRepository.deleteTaskRecord(recordId);
  }

  public List<FeatureHistoryBO>  getTaskFeatureHistories(String recordId) {
    return  featureHistoryService.getHistories(recordId);
  }

  public List<HistoryNodeDto> getTaskFeatureHistoryTree(String recordId) {
    List<FeatureHistoryBO> histories = featureHistoryService.getHistories(recordId);
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    Map<String, FeatureHistoryBO> historyMap = histories.stream()
        .collect(Collectors.toMap(FeatureHistoryBO::getFeatureId, history -> history));
    TaskRecordBO taskRecord = getTaskRecord(recordId);
    String testCaseId = taskRecord.getTestCaseId();
    List<FeatureInfoBO> featureInfos = featureService.queryFeatureList(testCaseId);
    List<HistoryNodeDto> historyNodes = featureInfos.stream().map(feature -> {
      HistoryNodeDto historyNodeDto = new HistoryNodeDto();
      historyNodeDto.setParentId(feature.getParentId());
      historyNodeDto.setRecordId(recordId);
      historyNodeDto.setSkip(Objects.equals(feature.getStatus(), FeatureStatus.DISABLE.getType()));
      historyNodeDto.setFeatureId(feature.getFeatureId());
      historyNodeDto.setFeatureName(feature.getFeatureName());
      FeatureHistoryBO featureHistory = historyMap.get(feature.getFeatureId());
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

  public TaskRecordBO getTaskRecord(String recordId) {
    return taskRecordRepository.getTaskRecord(recordId);
  }

  public List<TaskRecordBO> getTaskRecordsByTaskId(String taskId) {
    return taskRecordRepository.getTaskRecordsOrderByTime(taskId);
  }

  public Boolean stopTaskRecord(String recordId) {
    //todo notify master to stop dispatch task
    return taskRecordRepository.deleteTaskRecord(recordId);
  }

  public TaskRecordBO getTaskRecordByTrigger(String triggerId) {
    return taskRecordRepository.getTaskRecordByTrigger(triggerId);
  }

  public PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer page, Integer size) {
    return taskRecordRepository.getTriggerTaskRecords(triggerId, page, size);
  }
}
