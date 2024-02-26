package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum TemplateType {
  DEFAULT(1),
  CONDITION(2),
  CUSTOM(3);
  private final int type;

  TemplateType(int type) {
    this.type = type;
  }

}
