package com.zj.pipeline.entity.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/22
 */
@Data
public class PipelineNodeDTO {
  /**
   * 节点Id
   * */
  private String nodeId;

  /**
   * 流水线阶段Id
   * */
  private String stageId;

  /**
   * 节点名称
   * */
  @NotEmpty
  private String nodeName;

  /**
   * 节点类型
   * */
  private Integer type;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 配置信息
   * */
  private String configDetail;
}
