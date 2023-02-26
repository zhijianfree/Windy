package com.zj.feature.executor.compare.operator;

import com.zj.common.Constant;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.executor.compare.CompareOperator;
import com.zj.feature.executor.compare.CompareResult;

public abstract class BaseCompare implements CompareOperator {

    public CompareResult createSuccessResult(){
        CompareResult result = new CompareResult();
        result.setCompareStatus(true);
        result.setErrorMessage(ErrorCode.SUCCESS.getMessage());
        return result;
    }
}
