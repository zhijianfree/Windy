package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.feature.entity.dto.HistoryNodeDTO;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.po.feature.TaskRecord;
import com.zj.domain.mapper.feeature.TaskRecordMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TaskRecordService {

  @Autowired
  private FeatureHistoryService featureHistoryService;

  @Autowired
  private ICacheService cacheService;

  @Autowired
  private FeatureService featureService;

  @Autowired
  private TestCaseService testCaseService;

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

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

  public List<HistoryNodeDTO> getTaskResult(String recordId) {
    List<FeatureHistoryDto> histories = featureHistoryService.getHistories(recordId);
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    Map<String, FeatureHistoryDto> historyMap = histories.stream()
        .collect(Collectors.toMap(FeatureHistoryDto::getFeatureId, history -> history));
    TaskRecordDto record = getTaskRecord(recordId);
    String testCaseId = record.getTestCaseId();
    List<FeatureInfoDto> featureInfos = featureService.queryFeatureList(testCaseId);
    List<HistoryNodeDTO> historyNodeDTOS = featureInfos.stream().map(feature -> {
      HistoryNodeDTO historyNodeDTO = new HistoryNodeDTO();
      historyNodeDTO.setParentId(feature.getParentId());
      historyNodeDTO.setRecordId(recordId);
      historyNodeDTO.setFeatureId(feature.getFeatureId());
      historyNodeDTO.setFeatureName(feature.getFeatureName());
      FeatureHistoryDto featureHistory = historyMap.get(feature.getFeatureId());
      if (Objects.nonNull(featureHistory)) {
        historyNodeDTO.setHistoryId(featureHistory.getHistoryId());
        historyNodeDTO.setExecuteStatus(historyNodeDTO.getExecuteStatus());
      }
      return historyNodeDTO;
    }).collect(Collectors.toList());

    HistoryNodeDTO root = new HistoryNodeDTO();
    convertTree(historyNodeDTOS, root);
    return root.getChildren();
  }

  private void convertTree(List<HistoryNodeDTO> featureList, HistoryNodeDTO parent) {
    if (CollectionUtils.isEmpty(featureList)) {
      return;
    }

    List<HistoryNodeDTO> list = featureList.stream()
        .filter(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()))
        .collect(Collectors.toList());
    parent.setChildren(list);

    featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
    list.forEach(node -> {
      convertTree(featureList, node);
    });
  }

  public TaskRecordDto getTaskRecord(String recordId) {
    return taskRecordRepository.getTaskRecord(recordId);
  }

  public List<TaskRecordDto> getTaskRecordsByTaskId(String taskId) {
    return taskRecordRepository.getTaskRecordsOrderByTime(taskId);
  }
}
