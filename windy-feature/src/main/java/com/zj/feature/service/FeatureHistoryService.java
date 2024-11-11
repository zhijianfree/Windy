package com.zj.feature.service;

import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FeatureHistoryService {
  private final IFeatureHistoryRepository featureHistoryRepository;

  public FeatureHistoryService(IFeatureHistoryRepository featureHistoryRepository) {
    this.featureHistoryRepository = featureHistoryRepository;
  }

  public List<FeatureHistoryBO> featureHistories(String featureId) {
    return featureHistoryRepository.featureHistories(featureId);
  }

  public boolean deleteHistory(String historyId) {
    return featureHistoryRepository.deleteHistory(historyId);
  }

  public List<FeatureHistoryBO> getHistories(String taskId) {
    return featureHistoryRepository.getHistoriesByTaskRecordId(taskId);
  }

  public boolean deleteByRecordId(String taskRecordId) {
    return featureHistoryRepository.deleteByRecordId(taskRecordId);
  }
}
