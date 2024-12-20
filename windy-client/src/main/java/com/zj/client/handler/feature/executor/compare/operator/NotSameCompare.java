package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NotSameCompare extends BaseCompare {

    public static final String COMPARE_ERROR_ERROR_FORMAT = "response data=%s is same as expect value=%s";


    @Override
    public CompareType getType() {
        return CompareType.SAME;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        if (Objects.equals(String.valueOf(compareDefine.getResponseValue()), compareDefine.getExpectValue())) {
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage(String.format(COMPARE_ERROR_ERROR_FORMAT, compareDefine.getResponseValue(),
                    compareDefine.getExpectValue()));
        }
        return compareResult;
    }
}
