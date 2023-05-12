package com.zj.client.feature.executor.compare.operator;

import com.zj.client.feature.executor.compare.CompareOperator;
import com.zj.client.feature.executor.compare.CompareResult;
import com.zj.common.exception.ErrorCode;

public abstract class BaseCompare implements CompareOperator {

    public CompareResult createSuccessResult(){
        CompareResult result = new CompareResult();
        result.setCompareStatus(true);
        result.setErrorMessage(ErrorCode.SUCCESS.getMessage());
        return result;
    }
}
