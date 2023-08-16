package com.zj.domain.entity.enums;

import lombok.Getter;
@Getter
public enum SourceStatus {
  AVAILABLE(1, "可用状态"),
  UNAVAILABLE(2, "不可用状态");


  private final Integer type;
  private final String desc;

  SourceStatus(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

}
