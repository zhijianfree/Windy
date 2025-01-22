package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.client.utils.CompareJsonUtils;
import com.zj.common.exception.ErrorCode;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JSONCompare extends BaseCompare {
    @Override
    public CompareType getType() {
        return CompareType.JSON;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        try {
            Map<String, Object> result = CompareJsonUtils.compareJsonObject(
                    JSON.toJSONString(compareDefine.getResponseValue()), compareDefine.getExpectValue());
            if (MapUtils.isNotEmpty(result)){
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                compareResult.setErrorMessage("JSON compare error");
            }
        }catch (Exception e){
            log.info("json compare error", e);
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("JSON compare error");
        }
        return compareResult;
    }
}
