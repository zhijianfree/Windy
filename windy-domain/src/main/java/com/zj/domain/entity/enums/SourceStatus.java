package com.zj.domain.entity.enums;

public enum SourceStatus {
  AVAILABLE(1, "可用状态"),
  UNAVAILABLE(2, "不可用状态");

  private Integer type;
  private String desc;

  SourceStatus(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
