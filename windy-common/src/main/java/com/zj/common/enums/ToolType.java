package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum ToolType {
  JAVA("Java", "以Java作为开发语言"),
  GO("Go", "以Go作为开发语言"),
  MAVEN("Maven", "Maven版本"),
  PYTHON("Python", "以Python作为开发语言"),;

  ToolType(String type, String desc) {
    this.desc = desc;
    this.type = type;
  }

  private final String desc;

  private final String type;

}
