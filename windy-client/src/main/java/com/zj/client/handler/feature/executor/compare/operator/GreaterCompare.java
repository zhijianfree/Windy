package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import com.zj.common.feature.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.common.enums.CompareType;

@Component
public class GreaterCompare extends BaseCompare {

  @Override
  public CompareType getType() {
    return CompareType.GREATER;
  }

  @Override
  public CompareResult compare(CompareDefine compareDefine) {
    CompareResult compareResult = createSuccessResult();
    String responseValue = String.valueOf(compareDefine.getResponseValue());
    try {
      if (Long.parseLong(responseValue) <= Integer.parseInt(compareDefine.getExpectValue())) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    } catch (Exception e) {
      if (Double.parseDouble(responseValue) <= Double.parseDouble(compareDefine.getExpectValue())) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    }
    return compareResult;
  }
}
