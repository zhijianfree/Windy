package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum TaskRecordType {
    FEATURE_TASK(1),
    TEMP_TASK(2);

    TaskRecordType(Integer type) {
        this.type = type;
    }

    private final Integer type;
}
