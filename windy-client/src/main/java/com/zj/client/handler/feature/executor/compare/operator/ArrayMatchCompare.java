package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import com.zj.common.feature.CompareDefine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ArrayMatchCompare extends BaseCompare{

    public static final String SELF = "self";
    private final OgnlDataParser ognlDataParser = new OgnlDataParser();
    @Override
    public CompareType getType() {
        return CompareType.ARRAY_ITEM_MATCH;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        try {
            List<Object> responseList = (List<Object>)compareDefine.getResponseValue();
            String compareKey = compareDefine.getCompareKey();
            String[] strings = compareKey.split("\\.");
            String propertyName = strings[strings.length - 1];
            log.info("compare key = {} response value={}", propertyName, JSON.toJSONString(compareDefine.getExpectValue()));
            boolean anyMatch = responseList.stream().anyMatch(obj -> {
                if (Objects.equals(propertyName, SELF)) {
                    return Objects.equals(compareDefine.getExpectValue(), String.valueOf(obj));
                }
                Object value = ognlDataParser.exchangeOgnlParamValue(obj, "$body." + propertyName);
                return Objects.equals(compareDefine.getExpectValue(), String.valueOf(value));
            });
            if (!anyMatch){
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                String message = String.format("not find expect value=%s in array", compareDefine.getExpectValue());
                compareResult.setErrorMessage(message);
            }
        }catch (Exception e){
            log.info("run array_item_match error", e);
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("array match run error");
        }
        return compareResult;
    }
}
