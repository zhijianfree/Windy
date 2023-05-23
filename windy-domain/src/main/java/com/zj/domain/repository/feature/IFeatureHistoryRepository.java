package com.zj.domain.repository.feature;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IFeatureHistoryRepository {

  List<FeatureHistoryDto> featureHistories(String featureId);

  boolean saveHistory(FeatureHistoryDto featureHistory);

  boolean deleteHistory(String historyId);

  List<FeatureHistoryDto> getHistoriesByTaskRecordId(String taskRecordId);

  boolean deleteByRecordId(String taskId);

  boolean updateStatus(String historyId, int status);

  List<FeatureHistoryDto> getTaskRecordFeatures(String taskRecordId);

  void stopTaskFeatures(String taskRecordId, ProcessStatus stop);
}
