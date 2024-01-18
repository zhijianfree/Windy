package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NotEmptyCompare extends BaseCompare{
    @Override
    public CompareType getType() {
        return CompareType.NOT_EMPTY;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        if (StringUtils.isBlank(String.valueOf(compareDefine.getResponseValue()))) {
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("response value is empty");
        }
        return compareResult;
    }
}
