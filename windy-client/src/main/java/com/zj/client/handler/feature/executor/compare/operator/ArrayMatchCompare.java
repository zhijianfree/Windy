package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import com.zj.common.feature.CompareDefine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ArrayMatchCompare extends BaseCompare{

    private final Pattern pattern = Pattern.compile("\\{(.*?)\\}(.*)");
    private final OgnlDataParser ognlDataParser = new OgnlDataParser();
    @Override
    public CompareType getType() {
        return CompareType.ARRAY_ITEM_MATCH;
    }

    @Override
    public CompareResult compare(CompareDefine compareDefine) {
        CompareResult compareResult = createSuccessResult();
        try {
            List<Object> responseList = JSON.parseArray(JSON.toJSONString(compareDefine.getResponseValue()), Object.class);
            PattenEntry pattenEntry = convertEntry(compareDefine.getExpectValue());
            log.info("compare key = {} response value={}", pattenEntry.getPropertyKey(),
                    JSON.toJSONString(compareDefine.getResponseValue()));
            boolean anyMatch = responseList.stream().anyMatch(obj -> {
                if (StringUtils.isBlank(pattenEntry.propertyKey)) {
                    return Objects.equals(pattenEntry.getExpectValue(), String.valueOf(obj));
                }
                Object value = ognlDataParser.exchangeOgnlParamValue(obj, "$body." + pattenEntry.propertyKey);
                return Objects.equals(pattenEntry.getExpectValue(), String.valueOf(value));
            });
            if (!anyMatch){
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                String message = String.format("not find expect value=%s in array", pattenEntry.getExpectValue());
                compareResult.setErrorMessage(message);
            }
        }catch (Exception e){
            log.info("run array_item_match error", e);
            compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
            compareResult.setErrorMessage("array match run error");
        }
        return compareResult;
    }

    public PattenEntry convertEntry(String value) {
        Matcher matcher = pattern.matcher(value);
        PattenEntry pattenEntry = new PattenEntry();
        pattenEntry.setExpectValue(value);
        if (matcher.find()) {
            pattenEntry.setPropertyKey(matcher.group(1));
            pattenEntry.setExpectValue(matcher.group(2));
        }
        return pattenEntry;
    }

    @Data
    public static class PattenEntry{

        private String propertyKey;

        private String expectValue;
    }
}
