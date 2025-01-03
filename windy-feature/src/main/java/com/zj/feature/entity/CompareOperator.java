package com.zj.feature.entity;

import lombok.Data;

@Data
public class CompareOperator {

    /**
     * 比较操作符
     */
    private String operator;

    /**
     * 操作符描述
     */
    private String description;
}
