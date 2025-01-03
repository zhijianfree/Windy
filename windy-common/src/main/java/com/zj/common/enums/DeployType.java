package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum DeployType {
  SSH(1, "二进制ssh部署"),
  K8S(2, "K8S部署"),
  DOCKER(3, "Docker镜像部署"),;

  DeployType(Integer type, String desc) {
    this.desc = desc;
    this.type = type;
  }

  private final String desc;

  private final Integer type;

}
