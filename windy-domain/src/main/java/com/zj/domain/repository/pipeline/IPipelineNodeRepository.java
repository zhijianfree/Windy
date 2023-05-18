package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PipelineNodeDTO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineNodeRepository {

  PipelineNodeDTO getPipelineNode(String pipelineNodeId);

  List<PipelineNodeDTO> getPipelineNodes(String pipelineId);
}
