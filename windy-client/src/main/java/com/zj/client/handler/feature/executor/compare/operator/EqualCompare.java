package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        String responseValue = String.valueOf(compareDefine.getResponseValue());
        try {
            if (!Objects.equals(Long.parseLong(responseValue), Long.parseLong(compareDefine.getExpectValue()))) {
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                String message = String.format(NOT_MATCH_FORMAT, compareDefine.getExpectValue(),
                    compareDefine.getResponseValue());
                compareResult.setErrorMessage(message);
            }
        } catch (Exception e) {
            try {
                if (!Objects.equals(Double.parseDouble(responseValue), Double.parseDouble(compareDefine.getExpectValue()))) {
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
