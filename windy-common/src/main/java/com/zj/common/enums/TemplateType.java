package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum TemplateType {
  NORMAL(1),
  FOR(2),
  IF(3),
  DEFAULT(4),;
  private final int type;

  TemplateType(int type) {
    this.type = type;
  }

}
