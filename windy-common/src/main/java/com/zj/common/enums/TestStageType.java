package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum TestStageType {
    PRE_STAGE(1, "前置阶段执行点"),
    EXECUTE(2, "实际执行阶段"),
    CLEAN(3, "清理数据阶段"),;

    TestStageType(Integer type, String desc) {
        this.desc = desc;
        this.type = type;
    }

    private final String desc;

    private final Integer type;
}
