package com.zj.pipeline.executer;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/4/12
 */
public class CompareUtils {

  public static boolean isMatch(String operator, Object value, Object expectValue) {
    switch (operator) {
      case "equals":
        return Objects.equals(value, expectValue);
      case ">":
        int intValue = Integer.parseInt(String.valueOf(value));
        int intExpectValue = Integer.parseInt(String.valueOf(expectValue));
        return intValue > intExpectValue;
      case ">=":
        return Integer.parseInt(String.valueOf(value)) >= Integer.parseInt(
            String.valueOf(expectValue));
      case "<":
        return Integer.parseInt(String.valueOf(value)) < Integer.parseInt(
            String.valueOf(expectValue));
      case "<=":
        return Integer.parseInt(String.valueOf(value)) <= Integer.parseInt(
            String.valueOf(expectValue));
      default:
        return false;
    }
  }
}
