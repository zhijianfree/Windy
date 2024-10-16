package com.zj.client.entity.enuns;

public enum ExecutePointType {
  NORMAL(1, "普通用例"),
  FOR(2, "for循环用例"),
  IF(3, "if判断用例"),
  DEFAULT(4, "系统默认用例"),
  SCRIPT(6, "js脚本"),
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
