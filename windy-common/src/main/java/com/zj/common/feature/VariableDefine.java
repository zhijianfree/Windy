package com.zj.common.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/5
 */
@Data
public class VariableDefine {

  /**
   * 变量参数名
   * */
  private String variableKey;

  /**
   * 变量值，支持ognl
   * */
  private String variableValue;

}
