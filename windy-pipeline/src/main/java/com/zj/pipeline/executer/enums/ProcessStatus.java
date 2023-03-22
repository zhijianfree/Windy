package com.zj.pipeline.executer.enums;

public enum ProcessStatus {
  SUCCESS(1),
  FAIL(2),
  TIMEOUT(3),
  RUNNING(4),
  IGNORE_FAIL(5),;

  ProcessStatus(int type) {
    this.type = type;
  }

  private int type;

  public int getType() {
    return type;
  }
}
