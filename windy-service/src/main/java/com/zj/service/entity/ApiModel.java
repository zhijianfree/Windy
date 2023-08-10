package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiModel {

  private String apiId;

  /**
   * api名称
   */
  private String apiName;

  /**
   * 服务Id
   */
  private String serviceId;

  /**
   * 父节点Id
   */
  private String parentId;

  /**
   * api 类型 http、dubbo
   */
  private String type;

  private Boolean isApi;

  /**
   * http方法
   */
  private String method;

  /**
   * api信息 type = http时api内容为uri type = http时api内容为service#method
   */
  private String api;

  /**
   * api描述
   */
  private String description;

  private List<ApiRequest> requestParams;

  private List<ApiResponse> responseParams;
}
