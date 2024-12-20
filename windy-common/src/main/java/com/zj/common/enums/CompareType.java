package com.zj.common.enums;

import lombok.Getter;

@Getter
public enum CompareType {
    /** 字符串对比*/
    SAME("equal", "等值比对"),
    NOT_SAME("not_equal", "值不相同"),
    /** 字符串内容不能为空*/
    NOT_EMPTY("not_null", "值不为空"),
    /** 对象数组内容匹配*/
    ARRAY_ITEM_MATCH("array_item_match", "数组内容匹配"),
    /** 对象数组内容不存在*/
    NONE_ITEM_MATCH("none_item_match", "数组内容不存在"),
    /**枚举匹配*/
    ENUM_MATCH("enum_match", "枚举匹配(逗号隔开)"),
    /** json对比*/
    JSON("json_equal", "json对象比对"),
    /** json数组比较*/
    JSON_ARRAY("json_list_equal", "json数组比对"),
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
    ;
    private final String operator;

    private final String desc;

    CompareType(String operator, String desc) {
        this.operator = operator;
        this.desc = desc;
    }
}
