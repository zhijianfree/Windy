package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum InvokerType {
  METHOD(1, "本地方法调用"),
  HTTP(2, "Http调用"),
  RELATED_TEMPLATE(3, "关联模版调用"),;
  private final Integer type;
  private final String desc;

  InvokerType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

}
