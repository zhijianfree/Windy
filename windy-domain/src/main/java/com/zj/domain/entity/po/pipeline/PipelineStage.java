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
   * 阶段Id
   * */
  private String stageId;

  /**
   * 阶段名称
   * */
  private String stageName;

  /**
   * 节点类型
   * */
  private Integer type;

  /**
   * 排序
   */
  private Integer sortOrder;

  /**
   * 关联的配置Id
   * */
  private String configId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 创建时间
   * */
  private Long updateTime;

}
