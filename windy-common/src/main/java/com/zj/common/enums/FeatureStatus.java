package com.zj.common.enums;

public enum FeatureStatus {
    NORMAL(1),
    DISABLE(2);
    private Integer type;

    FeatureStatus(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
