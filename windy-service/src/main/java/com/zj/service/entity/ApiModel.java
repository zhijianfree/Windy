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
  private String resource;

  /**
   * api描述
   */
  private String description;

  private List<ApiRequestVariable> requestParams;

  private List<ApiResponse> responseParams;

  /**
   * 请求的body类名
   * */
  private String bodyClass;

  /**
   * 响应结果类名
   * */
  private String resultClass;

  /**
   * 代码生成的类名
   * */
  private String className;

  /**
   * 代码生成的类方法名
   * */
  private String classMethod;
}
