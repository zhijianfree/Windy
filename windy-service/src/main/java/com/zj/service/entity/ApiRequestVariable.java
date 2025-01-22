package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiRequestVariable {

  /**
   * API请求参数属性名
   */
  private String paramKey;

  /**
   * 请求参数数据类型 {@link com.zj.plugin.loader.ParamValueType}
   */
  private String type;

  /**
   * 请求参数位置 {@link com.zj.common.enums.Position}
   */
  private String position;

  /**
   * 请求参数描述
   */
  private String description;

  /**
   * 是否必填字段
   */
  private boolean isRequired;

  /**
   * 生成二方包时类名，只有type类型为Object时有效
   */
  private String objectName;

  /**
   * 参数默认值
   */
  private String defaultValue;

  /**
   * 数组类型时有效
   */
  private List<ApiRequestVariable> children;
}
