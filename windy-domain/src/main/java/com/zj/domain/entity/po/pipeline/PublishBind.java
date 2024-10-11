package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Data
public class PublishBind {

  private Long id;

  /**
   * 发布Id
   */
  private String publishId;

  /**
   * 发布分支
   */
  private String branch;

  /**
   * 服务Id
   */
  private String serviceId;

  /**
   * 流水线Id
   */
  private String pipelineId;

  /**
   * 发布分支变更内容
   */
  private String message;

  /**
   * 发布状态
   */
  private Integer status;

  private Long createTime;

  private Long updateTime;
}
