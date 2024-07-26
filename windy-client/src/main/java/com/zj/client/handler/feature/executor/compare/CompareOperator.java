package com.zj.client.handler.feature.executor.compare;

import com.zj.common.enums.CompareType;
import com.zj.common.feature.CompareDefine;

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
