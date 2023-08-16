package com.zj.domain.entity.dto.service;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Data
public class ServiceApiDto {

  private String apiId;

  /**
   * api名称
   * */
  private String apiName;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 父节点Id
   * */
  private String parentId;

  /**
   * api 类型 http、dubbo
   * */
  private String type;

  /**
   * 是否是API
   * */
  private Boolean isApi;

  /**
   * http方法
   * */
  private String method;

  /**
   * api信息
   * type = http时api内容为uri
   * type = http时api内容为service#method
   * */
  private String api;

  /**
   * api描述
   * */
  private String description;

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
