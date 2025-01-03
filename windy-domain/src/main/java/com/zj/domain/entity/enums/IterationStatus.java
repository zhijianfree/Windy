package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum IterationStatus {
    NOT_HANDLE(1),
    WORKING(2),
    CLOSED(3),
    COMPLETE(4)
    ;
    private final Integer type;

    IterationStatus(Integer type) {
        this.type = type;
    }

    public static List<IterationStatus> getNotHandleIterations() {
        return Arrays.asList(IterationStatus.NOT_HANDLE, IterationStatus.WORKING);
    }
}
