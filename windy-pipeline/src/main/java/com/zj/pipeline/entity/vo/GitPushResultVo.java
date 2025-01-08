package com.zj.pipeline.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
@Builder
public class GitPushResultVo {

  /**
   * 代码关联的服务ID
   */
  private String relatedServiceId;

  /**
   * git类型
   */
  private String gitType;

  /**
   * event类型，hook的事件类型
   */
  private String eventType;

  /**
   * git仓库地址
   */
  private String repository;

  /**
   * 事件关联的分支
   */
  private String branch;
}
