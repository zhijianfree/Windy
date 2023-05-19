package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface INodeRecordRepository {

  boolean saveNodeRecord(NodeRecordDto nodeRecord);

  boolean updateNodeRecord(NodeRecordDto nodeRecord);

  boolean updateNodeRecordStatus(String recordId, Integer status, String message);

  boolean batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus);

  List<NodeRecordDto> getRecordsByHistoryId(String historyId);

  NodeRecord getRecordById(String recordId);

  NodeRecordDto getRecordByNodeAndHistory(String historyId, String nodeId);
}
