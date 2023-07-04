package com.zj.pipeline.service;

import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/13
 */
@Service
public class PipelineNodeService {

  private IPipelineNodeRepository pipelineNodeRepository;

  public PipelineNodeService(IPipelineNodeRepository pipelineNodeRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
  }


  public void deleteNodeIds(List<String> nodeIds) {
    pipelineNodeRepository.deleteNodeIds(nodeIds);
  }

  public void updateNode(PipelineNodeDto dto) {
    pipelineNodeRepository.updateNode(dto);
  }

  public void deleteByPipeline(String pipelineId) {
    pipelineNodeRepository.deleteByPipelineId(pipelineId);
  }

  public void saveNode(PipelineNodeDto pipelineNode) {
    pipelineNodeRepository.saveNode(pipelineNode);
  }

  public List<PipelineNodeDto> getPipelineNodes(String pipelineId) {
    return pipelineNodeRepository.getPipelineNodes(pipelineId);
  }
}
