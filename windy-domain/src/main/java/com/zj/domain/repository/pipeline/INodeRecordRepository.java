package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface INodeRecordRepository {

  /**
   * 保存节点执行记录
   * @param nodeRecord 节点记录
   * @return 是否成功
   */
  boolean saveNodeRecord(NodeRecordBO nodeRecord);

  /**
   * 更新节点执行记录
   * @param nodeRecord 节点记录
   * @return 是否成功
   */
  boolean updateNodeRecord(NodeRecordBO nodeRecord);

  /**
   * 更新节点执行记录
   * @param recordId 记录ID
   * @param status 状态
   * @param messageList 消息列表
   * @return 是否成功
   */
  boolean updateNodeRecordStatus(String recordId, Integer status, List<String> messageList);

  /**
   * 获取节点执行记录
   * @param pipelineHistoryId 流水线历史ID
   * @return 节点记录列表
   */
  List<NodeRecordBO> getRecordsByHistoryId(String pipelineHistoryId);

  /**
   * 获取节点执行记录
   * @param nodeRecordId 记录ID
   * @return 节点记录
   */
  NodeRecordBO getRecordById(String nodeRecordId);

  /**
   * 获取流水线节点执行记录
   * @param pipelineHistoryId 流水线历史ID
   * @param nodeId 节点ID
   * @return 节点记录
   */
  NodeRecordBO getRecordsByNodeIdAndHistory(String pipelineHistoryId, String nodeId);

  /**
   * 更新节点执行记录状态
   * @param historyId 历史ID
   * @param stop 状态
   */
  void updateRunningNodeStatus(String historyId, ProcessStatus stop);

  /**
   * 删除流水线节点执行记录
     * @param historyId 流水线历史ID
   * @return 是否成功
   */
  boolean deleteRecordByHistoryId(String historyId);

  /**
   * 删除流水线节点执行记录
   * @param nodeIds 节点ID列表
   * @return 是否成功
   */
  boolean deleteRecordByNodeId(List<String> nodeIds);
}
