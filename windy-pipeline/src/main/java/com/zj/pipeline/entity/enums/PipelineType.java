package com.zj.pipeline.entity.enums;

/**
 * @author falcon
 * @since 2022/6/13
 */
public enum PipelineType {
  MAIN(1, "主流水线"),
  SCHEDULE(2, "定时流水线"),
  CUSTOM(3, "自定义流水线"),;

  private Integer type;
  private String desc;

  PipelineType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }
}
