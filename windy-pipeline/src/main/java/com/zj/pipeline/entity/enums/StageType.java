package com.zj.pipeline.entity.enums;

public enum StageType {
  START(1, "开始节点"),
  NORMAL(2, "一般节点"),
  AUDIT_WAIT(3, "等待审核"),
  END(4, "结束节点"),
  ;
  private Integer type;

  private String desc;

  StageType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }
}
