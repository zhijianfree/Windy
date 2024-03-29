package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.CompareType;
import com.zj.client.utils.CompareJsonUtils;
import com.zj.common.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/1/29
 */
@Component
public class JSONArrayCompare extends BaseCompare {

  @Override
  public CompareType getType() {
    return CompareType.JSON_ARRAY;
  }

  @Override
  public CompareResult compare(CompareDefine compareDefine) {
    CompareResult compareResult = createSuccessResult();
    List<Object> resList = JSON.parseArray(JSON.toJSONString(compareDefine.getResponseValue()),
        Object.class);
    List<Object> expectList = JSON.parseArray(compareDefine.getExpectValue(),
        Object.class);

    for (int i = 0; i < expectList.size(); i++) {
      Map<String, Object> result = CompareJsonUtils.compareJsonObject(
          JSON.toJSONString(resList.get(i)), JSON.toJSONString(expectList.get(i)));
      if (result.size() != 0) {
        compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
        compareResult.setErrorMessage("JSON compare error");
        return compareResult;
      }
    }

    return compareResult;
  }
}
