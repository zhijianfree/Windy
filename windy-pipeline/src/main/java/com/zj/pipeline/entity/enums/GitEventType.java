package com.zj.pipeline.entity.enums;

import lombok.Getter;

@Getter
public enum GitEventType {
    COMMIT("push", "代码提交");

    private final String type;
    private final String desc;

    GitEventType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
