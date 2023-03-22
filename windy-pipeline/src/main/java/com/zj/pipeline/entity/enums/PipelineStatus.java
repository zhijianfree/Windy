package com.zj.pipeline.entity.enums;

public enum PipelineStatus {
    NORMAL(1),
    DISABLED(0);

    PipelineStatus(Integer type) {
        this.type = type;
    }

    private Integer type;

    public Integer getType() {
        return type;
    }
}
