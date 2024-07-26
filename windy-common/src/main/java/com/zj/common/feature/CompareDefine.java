package com.zj.common.feature;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CompareDefine {
    /**
     * 对比的Key
     * */
    @NotBlank
    private String compareKey;
    /**
     * key转化后的响应的数据
     * */
    private Object responseValue;
    /**
     * 运算符
     * */
    @NotBlank
    private String operator;

    /**
     * 期待的值
     * */
    @NotBlank
    private String expectValue;
}
