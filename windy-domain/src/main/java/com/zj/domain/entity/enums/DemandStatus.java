package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum DemandStatus {
    NOT_HANDLE(1),
    ACCEPTED(2),
    WORKING(3),
    WAIT_TEST(4),
    PUBLISHED(5),
    REFUSED(6),
    ;
    private final Integer type;

    DemandStatus(Integer type) {
        this.type = type;
    }

    public static List<DemandStatus> getNotHandleDemands() {
        return Arrays.asList(DemandStatus.NOT_HANDLE, DemandStatus.WORKING, ACCEPTED, WAIT_TEST);
    }
}
