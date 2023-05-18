package com.zj.master.entity.enums;

/**
 * @author falcon
 * @since 2023/5/18
 */
public enum EventType {

  STOP(1, "停止执行");

  EventType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  private Integer type;

  private String desc;
}
