package com.zj.domain.entity.bo.pipeline;

import javax.validation.constraints.NotEmpty;

import com.zj.common.entity.pipeline.ConfigDetail;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/22
 */
@Data
public class PipelineNodeBO {
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
   * 排序
   */
  private Integer sortOrder;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 配置信息
   * */
  private ConfigDetail configDetail;
}
