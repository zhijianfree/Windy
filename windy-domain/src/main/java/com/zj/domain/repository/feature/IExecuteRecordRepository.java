package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecuteRecordRepository {

  /**
   * 获取执行记录
   * @param historyId 历史记录ID
   * @return 执行记录
   */
  List<ExecuteRecordBO> getExecuteRecords(String historyId);

  /**
   * 保存执行记录
   * @param executeRecord 执行记录
   * @return 是否成功
   */
  boolean saveRecord(ExecuteRecordBO executeRecord);

  /**
   * 更新执行记录的状态和结果
   * @param executeRecord 执行记录
   * @return 是否成功
   */
  boolean updateStatusAndResult(ExecuteRecordBO executeRecord);

  /**
   * 删除执行记录
   * @param historyId 历史记录ID
   * @return 是否成功
   */
  boolean deleteByHistoryId(String historyId);

  /**
   * 批量删除执行记录
   * @param historyIds 历史记录ID列表
   * @return 是否成功
   */
  boolean batchDeleteByHistoryId(List<String> historyIds);
}
