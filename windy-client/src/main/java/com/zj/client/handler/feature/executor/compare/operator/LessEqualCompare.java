package com.zj.client.handler.feature.executor.compare.operator;

import com.zj.common.exception.ErrorCode;
import com.zj.common.feature.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.common.enums.CompareType;
import org.springframework.stereotype.Component;

@Component
public class LessEqualCompare extends BaseCompare {

  @Override
  public CompareType getType() {
    return CompareType.LESS_EQUAL;
  }

  @Override
  public CompareResult compare(CompareDefine compareDefine) {
    CompareResult compareResult = createSuccessResult();
    String responseValue = String.valueOf(compareDefine.getResponseValue());
    try {
      if (Long.parseLong(responseValue) > Integer.parseInt(compareDefine.getExpectValue())) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    } catch (Exception e) {
      if (Double.parseDouble(responseValue) > Long.parseLong(compareDefine.getExpectValue())) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    }
    return compareResult;
  }
}
