package com.zj.client.entity.vo;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiModel {

  /**
   * api名称
   */
  private String apiName;

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

  private List<ApiRequest> responseParams;

  private String bodyClass;

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
