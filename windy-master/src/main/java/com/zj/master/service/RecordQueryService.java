package com.zj.master.service;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.common.entity.dto.ResponseStatusModel.PercentStatics;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.pipeline.NodeRecordDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/23
 */
@Service
public class RecordQueryService {

  private final INodeRecordRepository nodeRecordRepository;
  private final ITaskRecordRepository taskRecordRepository;
  private final IFeatureHistoryRepository featureHistoryRepository;

  public static final String FORMAT_TIPS = "任务执行状态: 成功数: %s 成功率百分比: %s";

  public RecordQueryService(INodeRecordRepository nodeRecordRepository,
      ITaskRecordRepository taskRecordRepository,
      IFeatureHistoryRepository featureHistoryRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.taskRecordRepository = taskRecordRepository;
    this.featureHistoryRepository = featureHistoryRepository;
  }

  public ResponseStatusModel getTaskStatus(String taskRecordId) {
    TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
    Integer status = taskRecord.getStatus();
    ResponseStatusModel responseStatusModel = new ResponseStatusModel();
    responseStatusModel.setStatus(status);

    List<FeatureHistoryBO> histories = featureHistoryRepository.getHistoriesByTaskRecordId(
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

  public NodeRecordDto getRecord(String recordId) {
    return nodeRecordRepository.getRecordById(recordId);
  }
}
