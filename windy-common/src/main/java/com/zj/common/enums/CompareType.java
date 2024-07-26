package com.zj.common.enums;

public enum CompareType {
    /** json对比*/
    JSON("json equal"),
    /** json数组比较*/
    JSON_ARRAY("json list equal"),
    /** 字符串对比*/
    SAME("equal"),
    /** 数字相等*/
    EQUAL("="),
    /** 数字大于*/
    GREATER(">"),
    /** 数字大于等于*/
    GREATER_EQUAL(">="),
    /** 数字小于*/
    LESS("<"),
    /** 数字小于等于*/
    LESS_EQUAL("<="),
    /** 字符串内容不能为空*/
    NOT_EMPTY("不为空"),
    ;
    private String operator;

    CompareType(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
