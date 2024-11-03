package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineNodeRepository {

  PipelineNodeBO getPipelineNode(String pipelineNodeId);

  List<PipelineNodeBO> getPipelineNodes(String pipelineId);

  List<PipelineNodeBO> getPipelineNodeByIds(List<String> nodeIds);

  boolean deleteNodeIds(List<String> nodeIds);

  void updateNode(PipelineNodeBO dto);

  boolean deleteByPipelineId(String pipelineId);

  void saveNode(PipelineNodeBO pipelineNode);
}
