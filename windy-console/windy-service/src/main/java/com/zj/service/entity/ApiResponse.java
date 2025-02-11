package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiResponse {

  /**
   * 响应参数属性名
   */
  private String paramKey;

  /**
   * 参数类型 {@link com.zj.plugin.loader.ParamValueType}
   */
  private String type;

  /**
   * 响应参数描述
   */
  private String description;

  /**
   * 是否为必须
   */
  private boolean required;

  /**
   * 生成二方包时响应参数的类名，参数类型为Object时有效
   */
  private String objectName;

  /**
   * 参数类型为Array时有效
   */
  private List<ApiResponse> children;
}
