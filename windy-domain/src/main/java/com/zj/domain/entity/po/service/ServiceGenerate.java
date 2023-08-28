package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class ServiceGenerate {

  private Long id;

  private String generateId;

  /**
   * 服务Id
   */
  private String serviceId;

  /**
   * 打包的包名路径
   */
  private String packageName;

  /**
   * maven打包的版本
   */
  private String version;

  /**
   * jar包groupId
   */
  private String groupId;

  /**
   * jar包artifactId
   */
  private String artifactId;

  private Long createTime;

  private Long updateTime;
}
