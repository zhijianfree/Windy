package com.zj.common.enums;

public enum DemandStatus {
    CREATE(1),
    ;
    private Integer type;

    DemandStatus(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
