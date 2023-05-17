package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.entity.po.feature.ExecuteRecord;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/17
 */
public interface IExecuteRecordRepository {

  List<ExecuteRecordDto> getExecuteRecords(String historyId);

  boolean saveRecord(ExecuteRecordDto executeRecord);

  boolean deleteByHistoryId(String historyId);

  boolean batchDeleteByHistoryId(List<String> historyIds);
}
