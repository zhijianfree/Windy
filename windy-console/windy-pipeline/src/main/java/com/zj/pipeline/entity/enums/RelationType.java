package com.zj.pipeline.entity.enums;

import lombok.Getter;

@Getter
public enum RelationType {
    DEMAND(1),
    BUG(2),
    WORK(3);
    private final Integer type;

    RelationType(Integer type) {
        this.type = type;
    }
}
