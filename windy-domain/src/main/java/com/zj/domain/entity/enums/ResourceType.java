package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum ResourceType {
    URI(1),
    MENU(2);
    private final Integer type;

    ResourceType(Integer type) {
        this.type = type;
    }
}
