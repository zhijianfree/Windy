package com.zj.domain.repository.feature;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IFeatureHistoryRepository {

  /**
   * 根据历史ID获取用例历史记录
   * @param historyId 历史ID
   * @return 用例历史
   */
  FeatureHistoryBO getFeatureHistory(String historyId);

  /**
   * 获取用例历史记录列表
   * @param featureId 用例ID
   * @return 用例历史列表
   */
  List<FeatureHistoryBO> featureHistories(String featureId);

  /**
   * 保存用例历史记录
   * @param featureHistory 用例历史记录
   * @return 是否成功
   */
  boolean saveHistory(FeatureHistoryBO featureHistory);

  /**
   * 删除用例历史记录
   * @param historyId 历史ID
   * @return 是否成功
   */
  boolean deleteHistory(String historyId);

  /**
   * 查询任务的用例历史记录
   * @param taskRecordId 任务ID
   * @return 是否成功
   */
  List<FeatureHistoryBO> getHistoriesByTaskRecordId(String taskRecordId);

  /**
   * 删除任务的用例历史记录
   * @param taskRecordId 任务ID
   * @return 是否成功
   */
  boolean deleteByRecordId(String taskRecordId);

  /**
   * 更新用例历史记录状态
   * @param historyId 历史ID
   * @param status 状态
   * @return 是否成功
   */
  boolean updateStatus(String historyId, int status);

  /**
   * 更新用例历史记录状态
   * @param taskRecordId 任务记录ID
   * @param processStatus 状态
   * @return 是否成功
   */
  boolean stopTaskFeatures(String taskRecordId, ProcessStatus processStatus);

  /**
   * 获取指定时间之前的用例历史记录
   * @param queryTime 查询时间
   * @return 历史记录
   */
  List<FeatureHistoryBO> getOldFeatureHistory(long queryTime);
}
