package com.zj.domain.entity.enums;

public enum ExecutePointType {
  NORMAL(1, "普通用例"),
  FOR(2, "for循环用例"),
  IF(3, "if判断用例"),
  COMMENT(4, "提示用例"),
  ;
  private final Integer type;
  private final String desc;

  ExecutePointType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
