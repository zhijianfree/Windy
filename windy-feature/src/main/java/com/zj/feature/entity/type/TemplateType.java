package com.zj.feature.entity.type;

public enum TemplateType {
  DEFAULT(1),
  SPECIAL(2);
  private int type;

  TemplateType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
