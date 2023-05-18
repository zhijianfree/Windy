package com.zj.common.enums;

public enum LogType {
  PIPELINE(1, "流水线"),
  FEATURE(2, "测试集"),
  FEATURE_TASK(3, "用例任务");
  private int type;
  private String desc;

  LogType(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public int getType() {
    return type;
  }
}
