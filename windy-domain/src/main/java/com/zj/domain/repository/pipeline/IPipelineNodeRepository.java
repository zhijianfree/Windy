package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineNodeRepository {

  PipelineNodeDto getPipelineNode(String pipelineNodeId);

  List<PipelineNodeDto> getPipelineNodes(String pipelineId);

  List<PipelineNodeDto> getPipelineNodeByIds(List<String> nodeIds);

  boolean deleteNodeIds(List<String> nodeIds);

  void updateNode(PipelineNodeDto dto);

  boolean deleteByPipelineId(String pipelineId);

  void saveNode(PipelineNodeDto pipelineNode);
}
