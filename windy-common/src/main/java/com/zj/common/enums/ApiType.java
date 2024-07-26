package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum ApiType {
    DIR(0),
    API(1),
    DISABLE(2),;

    ApiType(Integer type) {
        this.type = type;
    }

    private final Integer type;

}
