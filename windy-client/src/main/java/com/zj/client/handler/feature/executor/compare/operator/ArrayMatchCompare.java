package com.zj.client.handler.feature.executor.compare.operator;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.common.entity.WindyConstants;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ArrayMatchCompare extends BaseCompare {

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
            List<Object> responseList = JSON.parseArray(JSON.toJSONString(compareDefine.getResponseValue()),
                    Object.class);
            if (CollectionUtils.isEmpty(responseList)) {
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                compareResult.setErrorMessage("compare array list is empty");
                return compareResult;

            }
            PattenEntry pattenEntry = convertEntry(compareDefine.getExpectValue());
            log.info("compare key = {} response value={}", pattenEntry.getPropertyKey(),
                    JSON.toJSONString(compareDefine.getResponseValue()));
            boolean anyMatch = responseList.stream().anyMatch(obj -> {
                if (StringUtils.isBlank(pattenEntry.propertyKey)) {
                    return Objects.equals(pattenEntry.getExpectValue(), String.valueOf(obj));
                }
                Object value = ognlDataParser.exchangeOgnlResponseValue(obj,
                        WindyConstants.RESPONSE_BODY + pattenEntry.getPropertyKey());
                log.info("array item propertyKey={} value={} expectValue = {}", pattenEntry.getPropertyKey(),
                        value, pattenEntry.getExpectValue());
                return Objects.equals(pattenEntry.getExpectValue(), String.valueOf(value));
            });
            if (!anyMatch) {
                compareResult.setErrorType(ErrorCode.COMPARE_ERROR);
                String message = String.format("not find expect value=%s in array", pattenEntry.getExpectValue());
                compareResult.setErrorMessage(message);
            }
        } catch (Exception e) {
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
    public static class PattenEntry {

        private String propertyKey;

        private String expectValue;
    }

    public static void main(String[] args) {
        CompareDefine compareDefine = new CompareDefine();
        compareDefine.setResponseValue(JSON.parseArray("[{\"msg\":\"sssssssss;\",\"nickName\":\"云边协同应用\"}]"));
        compareDefine.setOperator("");
        compareDefine.setExpectValue("{msg}sssssssss;");
        ArrayMatchCompare arrayMatchCompare = new ArrayMatchCompare();
        CompareResult compareResult = arrayMatchCompare.compare(compareDefine);
        log.info(JSON.toJSONString(compareResult));
    }
}
