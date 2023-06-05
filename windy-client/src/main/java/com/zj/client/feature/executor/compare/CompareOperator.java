package com.zj.client.feature.executor.compare;

public interface CompareOperator {

    /**
     * 比较类型
     * */
    CompareType getType();

    /**
     * 开始比较
     * */
    CompareResult compare(CompareDefine compareDefine);
}
