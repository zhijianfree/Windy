package com.zj.client.feature.executor.feature.invoke;

public enum InvokerType {
  METHOD(1, "本地方法调用"),
  HTTP(2, "Http调用");
  private Integer type;
  private String desc;

  InvokerType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
