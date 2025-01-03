package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareOperator;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.exception.ErrorCode;

public abstract class BaseCompare implements CompareOperator {

    public CompareResult createSuccessResult(){
        CompareResult result = new CompareResult();
        result.setCompareSuccess(true);
        result.setErrorMessage(ErrorCode.SUCCESS.getMessage());
        return result;
    }
}
