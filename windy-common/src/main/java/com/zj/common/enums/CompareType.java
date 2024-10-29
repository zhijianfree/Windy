package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum CompareType {
    /** 字符串对比*/
    SAME("equal", "等值比对"),
    ARRAY_ITEM_MATCH("array_item_match", "数组内容匹配"),
    /** json对比*/
    JSON("json equal", "json对象比对"),
    /** json数组比较*/
    JSON_ARRAY("json list equal", "json数组比对"),
    /** 数字相等*/
    EQUAL("=", "等于"),
    /** 数字大于*/
    GREATER(">", "大于"),
    /** 数字大于等于*/
    GREATER_EQUAL(">=", "大于等于"),
    /** 数字小于*/
    LESS("<", "小于"),
    /** 数字小于等于*/
    LESS_EQUAL("<=", "小于等于"),
    /** 字符串内容不能为空*/
    NOT_EMPTY("不为空", "值不为空"),
    ;
    private final String operator;

    private final String desc;

    CompareType(String operator, String desc) {
        this.operator = operator;
        this.desc = desc;
    }
}
