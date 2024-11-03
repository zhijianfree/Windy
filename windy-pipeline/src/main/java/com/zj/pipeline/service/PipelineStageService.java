package com.zj.pipeline.service;

import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import com.zj.domain.repository.pipeline.IPipelineStageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/13
 */
@Service
public class PipelineStageService{

  private final IPipelineStageRepository pipelineStageRepository;

  public PipelineStageService(IPipelineStageRepository pipelineStageRepository) {
    this.pipelineStageRepository = pipelineStageRepository;
  }

  public void deletePipelineStages(List<String> notExistStages) {
    pipelineStageRepository.deletePipelineStages(notExistStages);
  }

  public PipelineStageBO getPipelineStage(String stageId) {
    return pipelineStageRepository.getPipelineStage(stageId);
  }

  public boolean updateStage(PipelineStageBO stageDto) {
    return pipelineStageRepository.updateStage(stageDto);
  }

  public boolean deleteStagesByPipelineId(String pipelineId) {
    return pipelineStageRepository.deleteStagesByPipelineId(pipelineId);
  }

  public void saveStage(PipelineStageBO pipelineStage) {
    pipelineStageRepository.saveStage(pipelineStage);
  }

  public List<PipelineStageBO> sortPipelineNodes(String pipelineId) {
    return pipelineStageRepository.sortPipelineNodes(pipelineId);
  }
}
