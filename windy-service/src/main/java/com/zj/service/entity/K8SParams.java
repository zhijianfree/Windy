package com.zj.service.entity;

import lombok.Data;

@Data
public class K8SParams {

  /**
   * k8s访问地址
   * */
  private String url;

  /**
   * k8s访问token
   * */
  private String token;

  /**
   * k8s镜像仓库地址
   * */
  private String repository;

  /**
   * 命名空间
   * */
  private String namespace;
}
