package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface INodeRecordRepository {

  boolean saveNodeRecord(NodeRecordBO nodeRecord);

  boolean updateNodeRecord(NodeRecordBO nodeRecord);

  boolean updateNodeRecordStatus(String recordId, Integer status, List<String> messageList);

  boolean batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus);

  List<NodeRecordBO> getRecordsByHistoryId(String historyId);

  NodeRecordBO getRecordById(String recordId);

  NodeRecordBO getRecordByNodeAndHistory(String historyId, String nodeId);

  void updateRunningNodeStatus(String historyId, ProcessStatus stop);

  boolean deleteRecordByHistoryId(String historyId);
}
