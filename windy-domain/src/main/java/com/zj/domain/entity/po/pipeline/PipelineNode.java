package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/13
 */
@Data
public class PipelineNode {

  private Long id;

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
  private String nodeName;

  /**
   * 节点类型
   * */
  private Integer type;

  private Integer sortOrder;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 配置信息
   * */
  private String configDetail;

  private Long createTime;

  private Long updateTime;
}
