package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum WorkTaskStatus {
    NOT_HANDLE(1),
    WORKING(2),
    COMPLETE(3),
    PAUSE(4)
    ;
    private final Integer type;

    WorkTaskStatus(Integer type) {
        this.type = type;
    }

    public static List<WorkTaskStatus> getNotHandleWorks() {
        return Arrays.asList(NOT_HANDLE, WORKING);
    }
}
