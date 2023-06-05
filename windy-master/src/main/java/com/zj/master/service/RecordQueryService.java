package com.zj.master.service;

import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.ResponseStatusModel;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/23
 */
@Service
public class RecordQueryService {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  public static final String FORMAT_TIPS = "任务执行状态: 成功数: %s 成功率百分比: %s";

  public ResponseStatusModel getTaskStatus(String taskRecordId) {
    TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
    Integer status = taskRecord.getStatus();
    ResponseStatusModel responseStatusModel = new ResponseStatusModel();
    responseStatusModel.setStatus(status);

    List<FeatureHistoryDto> histories = featureHistoryRepository.getHistoriesByTaskRecordId(
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

  public NodeRecordDto getRecord(String recordId) {
    return nodeRecordRepository.getRecordById(recordId);
  }
}
