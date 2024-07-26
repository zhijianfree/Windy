package com.zj.common.feature;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author guyuelan
 * @since 2023/1/5
 */
@Data
public class VariableDefine {

  /**
   * 变量参数名
   * */
  @NotBlank
  private String variableKey;

  /**
   * 变量值，支持ognl
   * */
  @NotBlank
  private String variableValue;

  /**
   * 设置全局变量
   * */
  private boolean global;

}
