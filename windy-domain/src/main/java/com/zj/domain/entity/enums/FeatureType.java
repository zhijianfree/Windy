package com.zj.domain.entity.enums;

public enum FeatureType {
  ITEM("用例", 1),
  FOLDER("目录", 2);


  FeatureType(String desc, Integer type) {
    this.desc = desc;
    this.type = type;
  }

  private String desc;

  private Integer type;

  public Integer getType() {
    return type;
  }
}
