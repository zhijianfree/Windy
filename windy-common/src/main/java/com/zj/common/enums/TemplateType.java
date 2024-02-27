package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum TemplateType {
  CUSTOM(1),
  DEFAULT(2),
  PLUGIN(3);
  private final int type;

  TemplateType(int type) {
    this.type = type;
  }

}
