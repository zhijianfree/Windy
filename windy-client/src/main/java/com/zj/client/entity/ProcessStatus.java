package com.zj.client.entity;

public enum ProcessStatus {
  SUCCESS(1, "处理成功"),
  FAIL(2, "处理失败"),
  TIMEOUT(3, "处理超时"),
  RUNNING(4, "运行中"),
  IGNORE_FAIL(5, "忽略失败"),

  STOP(6, "停止运行"),;

  ProcessStatus(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  private final int type;
  private final String desc;

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }
}
