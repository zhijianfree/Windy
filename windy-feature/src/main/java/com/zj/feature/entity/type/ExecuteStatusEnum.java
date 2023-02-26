package com.zj.feature.entity.type;

public enum ExecuteStatusEnum {
    INIT(0),
    RUNNING(1),
    TIMEOUT(2),
    SUCCESS(3),
    FAILED(4);

    private int status;

    ExecuteStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
