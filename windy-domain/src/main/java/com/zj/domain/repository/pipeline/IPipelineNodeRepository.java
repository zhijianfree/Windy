package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineNodeRepository {

  /**
   * 获取流水线节点
   * @param pipelineNodeId 节点ID
   * @return 节点信息
   */
  PipelineNodeBO getPipelineNode(String pipelineNodeId);

  /**
   * 获取流水线节点
   * @param pipelineId 流水线ID
   * @return 节点列表
   */
  List<PipelineNodeBO> getPipelineNodes(String pipelineId);

  /**
   * 删除流水线节点
   * @param nodeIds 节点ID列表
   * @return 是否成功
   */
  boolean deleteNodeIds(List<String> nodeIds);

  /**
   * 更新流水线节点
   * @param dto 节点信息
   */
  void updateNode(PipelineNodeBO dto);

  /**
   * 删除流水线所有节点
   * @param pipelineId 流水线ID
   * @return 是否成功
   */
  boolean deleteByPipelineId(String pipelineId);

  /**
   * 保存流水线节点
   * @param pipelineNode 节点信息
   */
  void saveNode(PipelineNodeBO pipelineNode);
}
