package com.zj.domain.entity.bo.pipeline;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/22
 */
@Data
public class PipelineStageBO {

  /**
   * 阶段ID
   */
  private String stageId;

  /**
   * 阶段名称
   */
  @NotEmpty
  private String stageName;

  /**
   * 阶段类型
   */
  private Integer type;

  /**
   * 流水线ID
   */
  private String pipelineId;

  /**
   * 排序
   */
  private Integer sortOrder;

  /**
   * 关联的配置Id
   * */
  private String configId;

  @NotEmpty
  private List<PipelineNodeBO> nodes;

}
