package com.zj.client.handler.feature.executor.invoker.invoke;

import lombok.Getter;

@Getter
public enum InvokerType {
  METHOD(1, "本地方法调用"),
  HTTP(2, "Http调用");
  private final Integer type;
  private final String desc;

  InvokerType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

}
