package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/13
 */
@Data
public class PipelineStage {

  private Long id;

  /**
   * 唯一Id
   * */
  private String stageId;

  /**
   * 阶段名称
   * */
  private String stageName;

  /**
   * 节点类型，排序时使用
   * */
  private Integer type;

  /**
   * 关联的配置Id
   * */
  private String configId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  private Long createTime;

  private Long updateTime;

}
