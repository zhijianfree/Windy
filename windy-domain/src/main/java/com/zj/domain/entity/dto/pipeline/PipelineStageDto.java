package com.zj.domain.entity.dto.pipeline;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/22
 */
@Data
public class PipelineStageDto {

  private String stageId;

  @NotEmpty
  private String stageName;

  private Integer type;

  private String pipelineId;

  /**
   * 关联的配置Id
   * */
  private String configId;

  @NotEmpty
  private List<PipelineNodeDto> nodes;

}
