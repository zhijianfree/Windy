package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.dto.FeatureHistoryDTO;
import com.zj.feature.entity.dto.FeatureNodeDTO;
import com.zj.feature.entity.dto.HistoryNodeDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.dto.TaskInfoDTO;
import com.zj.feature.entity.dto.TaskRecordDTO;
import com.zj.feature.entity.po.FeatureInfo;
import com.zj.feature.entity.po.TaskInfo;
import com.zj.feature.entity.po.TaskRecord;
import com.zj.feature.mapper.TaskRecordMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TaskRecordService extends ServiceImpl<TaskRecordMapper, TaskRecord> {

  @Autowired
  private FeatureHistoryService featureHistoryService;

  @Autowired
  private ICacheService cacheService;

  @Autowired
  private FeatureService featureService;

  @Autowired
  private TestCaseService testCaseService;

  public boolean insert(TaskRecordDTO taskRecordDTO) {
    TaskRecord taskRecord = new TaskRecord();
    BeanUtils.copyProperties(taskRecordDTO, taskRecord);
    return save(taskRecord);
  }

  public boolean updateRecordStatus(String recordId, int status) {
    TaskRecord taskRecord = new TaskRecord();
    taskRecord.setRecordId(recordId);
    taskRecord.setStatus(status);
    return saveOrUpdate(taskRecord,
        Wrappers.lambdaUpdate(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  public PageSize<TaskRecordDTO> getTaskRecordPage(Integer pageNum, Integer size) {
    IPage<TaskRecord> page = new Page<>(pageNum, size);
    IPage<TaskRecord> recordIPage = page(page,
        Wrappers.lambdaQuery(TaskRecord.class).orderByDesc(TaskRecord::getUpdateTime));

    PageSize<TaskRecordDTO> pageSize = new PageSize<>();
    pageSize.setTotal(recordIPage.getTotal());
    if (CollectionUtils.isEmpty(recordIPage.getRecords())) {
      pageSize.setData(Collections.emptyList());
      return pageSize;
    }

    List<TaskRecordDTO> dtoList = recordIPage.getRecords().stream().map(record -> {
      TaskRecordDTO taskInfoDTO = new TaskRecordDTO();
      BeanUtils.copyProperties(record, taskInfoDTO);
      return taskInfoDTO;
    }).collect(Collectors.toList());
    pageSize.setData(dtoList);
    return pageSize;
  }

  public boolean deleteTaskRecord(String recordId) {
    TaskRecord taskRecord = getOne(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
    boolean executeHistory = featureHistoryService.deleteByRecordId(taskRecord.getTestCaseId());
    boolean flag = remove(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
    return executeHistory && flag;
  }

  public List<HistoryNodeDTO> getTaskResult(String recordId) {
    List<FeatureHistoryDTO> histories = featureHistoryService.getHistories(recordId);
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    Map<String, FeatureHistoryDTO> historyMap = histories.stream()
        .collect(Collectors.toMap(FeatureHistoryDTO::getFeatureId, history -> history));
    TaskRecordDTO record = getTaskRecord(recordId);
    String testCaseId = record.getTestCaseId();
    List<FeatureInfo> featureInfos = featureService.queryFeatureList(testCaseId);
    List<HistoryNodeDTO> historyNodeDTOS = featureInfos.stream().map(feature -> {
      HistoryNodeDTO historyNodeDTO = new HistoryNodeDTO();
      historyNodeDTO.setParentId(feature.getParentId());
      historyNodeDTO.setRecordId(recordId);
      historyNodeDTO.setFeatureId(feature.getFeatureId());
      historyNodeDTO.setFeatureName(feature.getFeatureName());
      FeatureHistoryDTO featureHistory = historyMap.get(feature.getFeatureId());
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

  public TaskRecordDTO getTaskRecord(String recordId) {
    TaskRecord taskRecord = getOne(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
    if (Objects.isNull(taskRecord)) {
      return null;
    }

    TaskRecordDTO taskRecordDTO = new TaskRecordDTO();
    BeanUtils.copyProperties(taskRecord, taskRecordDTO);
    return taskRecordDTO;
  }
}
