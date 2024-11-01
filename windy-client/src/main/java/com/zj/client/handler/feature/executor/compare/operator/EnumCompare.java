package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import com.zj.common.feature.CompareDefine;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class EnumCompare extends BaseCompare {

    @Override
    public CompareType getType() {
        return CompareType.ENUM_MATCH;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        if (StringUtils.isBlank(compareDefine.getExpectValue())) {
            compareResult.setErrorMessage("expect value is empty");
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            return compareResult;
        }
        String responseValue = String.valueOf(compareDefine.getResponseValue());
        String[] strings = compareDefine.getExpectValue().split(",");
        boolean anyMatch = Arrays.asList(strings).contains(responseValue);
        if (!anyMatch) {
            compareResult.setErrorMessage("response data [" + compareDefine.getExpectValue() + "] is not int expect value ["
                    + compareDefine.getExpectValue() + "]");
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        }
        return compareResult;
    }
}
