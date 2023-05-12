package com.zj.client.feature.executor.compare.operator;

import com.zj.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import com.zj.client.feature.executor.compare.CompareDefine;
import com.zj.client.feature.executor.compare.CompareResult;
import com.zj.client.feature.executor.compare.CompareType;

@Component
public class GreaterCompare extends BaseCompare {

  @Override
  public CompareType getType() {
    return CompareType.GREATER;
  }

  @Override
  public CompareResult compare(CompareDefine compareDefine) {
    CompareResult compareResult = createSuccessResult();
    try {
      if (!(Long.parseLong(String.valueOf(compareDefine.getResponseValue())) > Integer.parseInt(
          compareDefine.getExpectValue()))) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    } catch (Exception e) {
      if (!(Double.parseDouble(String.valueOf(compareDefine.getResponseValue())) > Long.parseLong(
          compareDefine.getExpectValue()))) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("response value > expect value");
      }
    }
    return compareResult;
  }
}
