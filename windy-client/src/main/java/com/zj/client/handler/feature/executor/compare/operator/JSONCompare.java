package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.CompareType;
import com.zj.client.utils.CompareJsonUtils;
import com.zj.common.exception.ErrorCode;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JSONCompare extends BaseCompare {
    @Override
    public CompareType getType() {
        return CompareType.JSON;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        Map<String, Object> result = CompareJsonUtils.compareJsonObject(
                JSON.toJSONString(compareDefine.getResponseValue()), compareDefine.getExpectValue());
        if (result.size() != 0){
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("JSON compare error");
        }
        return compareResult;
    }
}
