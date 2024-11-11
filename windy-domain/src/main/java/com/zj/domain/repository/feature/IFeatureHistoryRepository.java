package com.zj.domain.repository.feature;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IFeatureHistoryRepository {

  FeatureHistoryBO getFeatureHistory(String historyId);

  List<FeatureHistoryBO> featureHistories(String featureId);

  boolean saveHistory(FeatureHistoryBO featureHistory);

  boolean deleteHistory(String historyId);

  List<FeatureHistoryBO> getHistoriesByTaskRecordId(String taskRecordId);

  boolean deleteByRecordId(String taskRecordId);

  boolean updateStatus(String historyId, int status);

  List<FeatureHistoryBO> getTaskRecordFeatures(String taskRecordId);

  void stopTaskFeatures(String taskRecordId, ProcessStatus stop);

    List<FeatureHistoryBO> getOldFeatureHistory(long queryTime);
}
