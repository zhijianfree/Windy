package com.zj.master.entity.enums;

import lombok.Getter;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Getter
public enum EventType {

  STOP(1, "停止执行"),
  RECOVER_CONTEXT(2, "覆盖全局变量"),;;

  EventType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  private Integer type;

  private String desc;
}
