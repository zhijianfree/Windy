package com.zj.client.entity.vo;

import lombok.Data;

@Data
public class GenerateConfig {
  /**
   * 打包的包名路径
   * */
  private String packageName;

  /**
   * maven打包的版本
   * */
  private String version;

  /**
   * jar包groupId
   * */
  private String groupId;

  /**
   * jar包artifactId
   * */
  private String artifactId;
}
