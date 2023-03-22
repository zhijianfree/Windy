package com.zj.pipeline.entity.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/22
 */
@Data
public class PipelineStageDTO {

  private String stageId;

  @NotEmpty
  private String stageName;

  private Integer type;

  private String pipelineId;

  @NotEmpty
  private List<PipelineNodeDTO> nodes;

}
