package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Data
public class PublishBindBO {

  /**
   * 发布ID
   */
  private String publishId;

  /**
   * 分支
   */
  private String branch;

  /**
   * 服务ID
   */
  private String serviceId;

  /**
   * 发布信息
   */
  private String message;

  /**
   * 流水线ID
   */
  private String pipelineId;

  /**
   * 发布状态
   */
  private Integer status;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 创建时间
   * */
  private Long updateTime;
}
