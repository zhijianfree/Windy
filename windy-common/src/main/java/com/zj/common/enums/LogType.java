package com.zj.common.enums;

import java.util.Arrays;
import java.util.Objects;

public enum LogType {
  PIPELINE(1, "流水线"),
  FEATURE(2, "测试集"),
  FEATURE_TASK(3, "用例任务"),
  GENERATE(4, "自动生成maven版本jar");
  private int type;
  private String desc;

  LogType(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public static LogType exchange(Integer type) {
    return Arrays.stream(LogType.values())
        .filter(logType -> Objects.equals(logType.getType(), type)).findFirst().orElse(null);
  }
}
