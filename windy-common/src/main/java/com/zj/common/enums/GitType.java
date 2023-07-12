package com.zj.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

public enum GitType {
  Gitlab("Private-Token"), Gitea("token");
  private String tokenName;

  GitType(String tokenName) {
    this.tokenName = tokenName;
  }

  public String getTokenName() {
    return tokenName;
  }

  public static GitType exchange(String name) {
    return Stream.of(GitType.values()).filter(type -> Objects.equals(type.name(), name)).findFirst()
        .orElse(GitType.Gitlab);
  }
}
