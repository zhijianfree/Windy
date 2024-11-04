package com.zj.client.entity.bo;

import java.util.List;
import lombok.Data;

@Data
public class ApiItem {

  /**
   * 请求的Uri
   */
  private String uri;

  /**
   * http方法
   */
  private String httpMethod;

  /**
   * 方法名
   */
  private String methodName;

  /**
   * 方法参数列表
   */
  private List<MethodParam> params;

  /**
   * 接口返回的class
   */
  private String resultClass;

  /**
   * body参数对应的对象名称
   */
  private String bodyClass;

  private String lowerBodyClass;

  @Data
  public static class MethodParam {

    /**
     * 参数位置
     * */
    private String position;

    /**
     * 参数名称
     * */
    private String name;

    /**
     * 参数类型
     * */
    private String type;

    /**
     * 参数是否是必须
     * */
    private String required;
  }

}
