package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.client.utils.CompareJsonUtils;
import com.zj.common.exception.ErrorCode;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/1/29
 */
@Slf4j
@Component
public class JSONArrayCompare extends BaseCompare {

  public static final String COMPARE_ERROR_MSG = "json array compare error";

  @Override
  public CompareType getType() {
    return CompareType.JSON_ARRAY;
  }

  @Override
  public CompareResult compare(CompareDefine compareDefine) {
    CompareResult compareResult = createSuccessResult();
    try {
      List<Object> resList = JSON.parseArray(JSON.toJSONString(compareDefine.getResponseValue()), Object.class);
      List<Object> expectList = JSON.parseArray(compareDefine.getExpectValue(), Object.class);
      if (expectList.size() > resList.size()) {
        log.info("json array compare expect list size more than response list size");
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage(COMPARE_ERROR_MSG);
        return compareResult;
      }
      for (int i = 0; i < expectList.size(); i++) {
        Map<String, Object> result = CompareJsonUtils.compareJsonObject(
                JSON.toJSONString(resList.get(i)), JSON.toJSONString(expectList.get(i)));
        if (!result.isEmpty()) {
          compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
          compareResult.setErrorMessage(COMPARE_ERROR_MSG);
          return compareResult;
        }
      }
    }catch (Exception e){
      log.info("array json compare error", e);
      compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
      compareResult.setErrorMessage(COMPARE_ERROR_MSG);
    }
    return compareResult;
  }
}
