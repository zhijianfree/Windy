package com.zj.common.monitor.log.dto;

public enum UpdateType {
  ALL(1),
  PACKAGE(2),
  CLASS(3);

  UpdateType(int type) {
    this.type = type;
  }

  public int type;

  public int getType() {
    return type;
  }
}
