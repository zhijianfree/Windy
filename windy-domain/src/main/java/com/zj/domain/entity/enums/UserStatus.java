package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    NORMAL(1),
    DISABLE(0);

    private int type;

    UserStatus(int type) {
        this.type = type;
    }
}
