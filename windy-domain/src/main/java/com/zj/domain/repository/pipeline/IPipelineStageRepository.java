package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface IPipelineStageRepository {

  void deletePipelineStages(List<String> stageIds);

  PipelineStageBO getPipelineStage(String stageId);

  boolean updateStage(PipelineStageBO stageDto);

  boolean deleteStagesByPipelineId(String pipelineId);

  void saveStage(PipelineStageBO pipelineStage);

  List<PipelineStageBO> sortPipelineNodes(String pipelineId);
}
