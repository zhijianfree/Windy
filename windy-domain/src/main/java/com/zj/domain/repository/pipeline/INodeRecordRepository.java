package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/15
 */
public interface INodeRecordRepository {

  boolean saveNodeRecord(NodeRecord nodeRecord);

  boolean updateNodeRecord(NodeRecord nodeRecord);

  boolean updateNodeRecordStatus(String recordId, Integer type, String message);

  boolean batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus);

  List<NodeRecord> getRecordsByHistoryId(String historyId);

  NodeRecord getRecordById(String recordId);
}
