package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum BugStatus {
    NOT_HANDLE(1),
    WORKING(2),
    TESTING(3),
    REJECT(4),
    PASS(5),
    PUBLISHED(6),
    ;
    private final Integer type;

    BugStatus(Integer type) {
        this.type = type;
    }

    public static List<BugStatus> getNotHandleBugs() {
        return Arrays.asList(NOT_HANDLE, WORKING, REJECT, TESTING);
    }
}
