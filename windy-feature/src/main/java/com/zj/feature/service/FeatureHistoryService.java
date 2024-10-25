package com.zj.feature.service;

import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeatureHistoryService {

  private final FeatureService featureService;
  private final IFeatureHistoryRepository featureHistoryRepository;

  public FeatureHistoryService(FeatureService featureService, IFeatureHistoryRepository featureHistoryRepository) {
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

  public boolean deleteByRecordId(String taskRecordId) {
    return featureHistoryRepository.deleteByRecordId(taskRecordId);
  }
}
