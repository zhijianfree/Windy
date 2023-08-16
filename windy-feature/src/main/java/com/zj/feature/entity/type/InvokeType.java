package com.zj.feature.entity.type;

public enum InvokeType {
  LOCAL_METHOD(1),
  HTTP(2);
  private int type;

  InvokeType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
