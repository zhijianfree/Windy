package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum MemberType {
    SERVICE_MEMBER("service"),
    ITERATION_MEMBER("iteration"),
    FEATURE_MEMBER("feature"),;

    private final String type;

    MemberType(String type) {
        this.type = type;
    }
}
