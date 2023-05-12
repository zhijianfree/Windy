package com.zj.master.entity.enums;

public enum LogType {
  PIPELINE(1, "流水线"),
  FEATURE(2, "用例");
  private int type;
  private String desc;

  LogType(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }
}
