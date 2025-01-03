package com.zj.common.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum GitType {
  Gitlab("Private-Token"),
  Gitea("token"),
  Github("");
  private final String tokenName;

  GitType(String tokenName) {
    this.tokenName = tokenName;
  }

  public static GitType exchange(String name) {
    return Stream.of(GitType.values()).filter(type -> Objects.equals(type.name(), name)).findFirst()
        .orElse(GitType.Gitlab);
  }
}
