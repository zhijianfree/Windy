package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.feature.CompareDefine;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SameCompare extends BaseCompare {
    @Override
    public CompareType getType() {
        return CompareType.SAME;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        if (!Objects.equals(String.valueOf(compareDefine.getResponseValue()), compareDefine.getExpectValue())) {
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("response data not same as expect value");
        }
        return compareResult;
    }
}
