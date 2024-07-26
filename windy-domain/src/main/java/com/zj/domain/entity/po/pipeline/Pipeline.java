package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class Pipeline {

  private Long id;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 流水线名称
   * */
  private String pipelineName;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 流水线类型
   * */
  private Integer pipelineType;

  /**
   * 执行方式
   * */
  private Integer executeType;

  /**
   * 流水线配置
   * */
  private String pipelineConfig;

  /**
   * 流水线状态
   * */
  private Integer pipelineStatus;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 更新时间
   * */
  private Long updateTime;


}
