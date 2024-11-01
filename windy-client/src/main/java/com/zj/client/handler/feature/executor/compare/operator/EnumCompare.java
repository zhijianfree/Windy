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
        String expectValue = String.valueOf(compareDefine.getExpectValue());
        if (StringUtils.isBlank(expectValue)) {
            compareResult.setErrorMessage("expect value is empty");
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            return compareResult;
        }
        String[] strings = expectValue.split(",");
        boolean anyMatch = Arrays.asList(strings).contains(expectValue);
        if (!anyMatch) {
            compareResult.setErrorMessage("response data [" + expectValue + "] is not int expect value ["
                    + compareDefine.getExpectValue() + "]");
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        }
        return compareResult;
    }
}
