package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum CaseType {
    NORMAL(1),
    E2E(2);

    CaseType(Integer type) {
        this.type = type;
    }

    private final Integer type;
}
