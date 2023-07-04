package com.zj.client.handler.feature.executor.compare;

import lombok.Data;

@Data
public class CompareDefine {
    /**
     * 对比的Key
     * */
    private String compareKey;
    /**
     * key转化后的响应的数据
     * */
    private Object responseValue;
    /**
     * 运算符
     * */
    private String operator;

    /**
     * 期待的值
     * */
    private String expectValue;
}
