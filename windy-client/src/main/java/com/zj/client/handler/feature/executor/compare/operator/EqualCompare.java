package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.CompareType;
import com.zj.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class EqualCompare extends BaseCompare {

    public static final String NOT_MATCH_FORMAT = "expect value %s actual value is %s";

    @Override
    public CompareType getType() {
        return CompareType.EQUAL;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        try {
            if (!(Long.parseLong(String.valueOf(compareDefine.getResponseValue())) == Long.parseLong(compareDefine.getExpectValue()))) {
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                String message = String.format(NOT_MATCH_FORMAT, compareDefine.getExpectValue(),
                    compareDefine.getResponseValue());
                compareResult.setErrorMessage(message);
            }
        } catch (Exception e) {
            try {
                if (!(Double.parseDouble(String.valueOf(compareDefine.getResponseValue())) != Double.parseDouble(compareDefine.getExpectValue()))) {
                    compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                    compareResult.setErrorMessage("response value not equal expect");
                }
            }catch (Exception ex){
                compareResult.setCompareStatus(false);
                String message = String.format(NOT_MATCH_FORMAT, compareDefine.getExpectValue(),
                    compareDefine.getResponseValue());
                compareResult.setErrorMessage(message);
            }

        }
        return compareResult;
    }
}
