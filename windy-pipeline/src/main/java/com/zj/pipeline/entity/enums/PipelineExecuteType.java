package com.zj.pipeline.entity.enums;

/**
 * @author falcon
 * @since 2022/6/13
 */
public enum PipelineExecuteType {
  MANUAL(1, "手动执行"),
  PUSH(2, "Push，代码commit"),
  MERGE(3, "merge代码merge"),;

  private Integer type;
  private String desc;

  PipelineExecuteType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
