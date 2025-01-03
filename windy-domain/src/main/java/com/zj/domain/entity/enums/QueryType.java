package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum QueryType {
    QUERY_ALL(1),
    QUERY_HANDLE_BY_MYSELF(2),
    QUERY_CREATE_BY_MYSELF(3),;

    QueryType(int type) {
        this.type = type;
    }

    private int type;
}
