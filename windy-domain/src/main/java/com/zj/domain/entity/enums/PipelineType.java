package com.zj.domain.entity.enums;

/**
 * @author guyuelan
 * @since 2022/6/13
 */
public enum PipelineType {
  PUBLISH(1, "主流水线"),
  SCHEDULE(2, "定时流水线"),
  CUSTOM(3, "自定义流水线"),;

  private Integer type;
  private String desc;

  PipelineType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
