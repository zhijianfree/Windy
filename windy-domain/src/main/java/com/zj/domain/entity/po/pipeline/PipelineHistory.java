package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class PipelineHistory {

  private Long id;

  /**
   * 历史Id
   * */
  private String historyId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 流水线名称
   * */
  private String pipelineName;

  /**
   * 流水线执行的分支
   * */
  private String branch;

  /**
   * 流水线执行人
   * */
  private String executor;

  /**
   * 流水线配置
   * */
  private String pipelineConfig;

  /**
   * 流水线结果
   * */
  private Integer pipelineStatus;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 创建时间
   * */
  private Long updateTime;
}
