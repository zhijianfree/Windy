package com.zj.domain.entity.po.service;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Data
public class ServiceApi {

  private Long id;

  private String apiId;

  /**
   * api名称
   * */
  private String apiName;

  /**
   * api 类型 http、dubbo
   * */
  private String type;

  /**
   * http方法
   * */
  private String method;

  /**
   * 父节点ID
   * */
  private String parentId;

  /**
   * api信息
   * type = http时api内容为uri
   * type = http时api内容为service#method
   * */
  private String api;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * api描述
   * */
  private String description;

  /**
   * 是否是API
   * */
  private Boolean isApi;

  /**
   * 请求参数
   * */
  private String requestParams;

  /**
   * 响应参数
   * */
  private String responseParams;

  private Long createTime;

  private Long updateTime;
}
