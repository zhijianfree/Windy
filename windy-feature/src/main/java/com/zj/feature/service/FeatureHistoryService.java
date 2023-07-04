package com.zj.feature.service;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.feature.entity.dto.FeatureInfoVo;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeatureHistoryService {

  private ExecuteRecordService executeRecordService;
  private FeatureService featureService;
  private IFeatureHistoryRepository featureHistoryRepository;

  public FeatureHistoryService(ExecuteRecordService executeRecordService,
      FeatureService featureService, IFeatureHistoryRepository featureHistoryRepository) {
    this.executeRecordService = executeRecordService;
    this.featureService = featureService;
    this.featureHistoryRepository = featureHistoryRepository;
  }

  public List<FeatureHistoryDto> featureHistories(String featureId) {
    return featureHistoryRepository.featureHistories(featureId);
  }

  public boolean deleteHistory(String historyId) {
    return featureHistoryRepository.deleteHistory(historyId);
  }

  public List<FeatureHistoryDto> getHistories(String taskId) {
    return featureHistoryRepository.getHistoriesByTaskRecordId(taskId);
  }

  public boolean deleteByRecordId(String taskId) {
    return featureHistoryRepository.deleteByRecordId(taskId);
  }

  public boolean updateStatus(String historyId, int status) {
    return featureHistoryRepository.updateStatus(historyId, status);
  }

  public void saveFeatureHistory(String featureId, String historyId, String recordId) {
    FeatureInfoVo featureInfo = featureService.getFeatureById(featureId);
    if (Objects.isNull(featureInfo)) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }

    FeatureHistoryDto featureHistory = new FeatureHistoryDto();
    featureHistory.setFeatureId(featureId);
    featureHistory.setExecuteStatus(ProcessStatus.RUNNING.getType());
    featureHistory.setHistoryId(historyId);
    featureHistory.setRecordId(recordId);
    featureHistory.setFeatureName(featureInfo.getFeatureName());
    featureHistoryRepository.saveHistory(featureHistory);
  }
}
