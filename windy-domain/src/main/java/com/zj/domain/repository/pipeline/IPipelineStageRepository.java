package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PipelineStageDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/19
 */
public interface IPipelineStageRepository {

  void deletePipelineStages(List<String> stageIds);

  PipelineStageDto getPipelineStage(String stageId);

  boolean updateStage(PipelineStageDto stageDto);

  boolean deleteStagesByPipelineId(String pipelineId);

  void saveStage(PipelineStageDto pipelineStage);

  List<PipelineStageDto> sortPipelineNodes(String pipelineId);
}
