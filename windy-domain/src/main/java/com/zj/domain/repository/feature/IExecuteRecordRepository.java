package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecuteRecordRepository {

  List<ExecuteRecordBO> getExecuteRecords(String historyId);

  boolean saveRecord(ExecuteRecordBO executeRecord);
  boolean updateStatusAndResult(ExecuteRecordBO executeRecord);

  boolean deleteByHistoryId(String historyId);

  boolean batchDeleteByHistoryId(List<String> historyIds);
}
