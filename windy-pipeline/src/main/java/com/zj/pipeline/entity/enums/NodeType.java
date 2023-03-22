package com.zj.pipeline.entity.enums;

public enum NodeType {
  build(1, "构建"),
  security(2, "安全"),
  deploy(3,"部署"),
  test(4, "测试"),
  Gray_deploy(5, "灰度部署"),
  ;
  private Integer type;

  private String desc;

  NodeType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
