package com.zj.domain.entity.enums;

public enum EnvType {
  SSH(1, "使用ssh部署的环境"),
  K8S(2, "使用k8s部署的环境"),
  DOCKER(3, "使用docker部署的环境"),;

  private Integer type;
  private String desc;

  EnvType(Integer type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public Integer getType() {
    return type;
  }
}
