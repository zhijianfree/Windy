package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.ExecuteRecordDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecuteRecordRepository {

  List<ExecuteRecordDto> getExecuteRecords(String historyId);

  boolean saveRecord(ExecuteRecordDto executeRecord);
  boolean updateStatusAndResult(ExecuteRecordDto executeRecord);

  boolean deleteByHistoryId(String historyId);

  boolean batchDeleteByHistoryId(List<String> historyIds);
}
