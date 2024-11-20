package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.bo.ExecutePoint;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.CompareOperator;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.compare.operator.CompareFactory;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.CompareResult;
import com.zj.common.entity.feature.ExecutePointDto;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.common.enums.TemplateType;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Component
public class IFExecuteStrategy extends BaseExecuteStrategy {

    private final OgnlDataParser ognlDataParser = new OgnlDataParser();
    private final CompareFactory compareFactory;

    public IFExecuteStrategy(InterceptorProxy interceptorProxy,
                             List<IExecuteInvoker> executeInvokers,
                             CompareHandler compareHandler, CompareFactory compareFactory) {
        super(interceptorProxy, executeInvokers, compareHandler);
        this.compareFactory = compareFactory;
    }

    @Override
    public List<TemplateType> getType() {
        return Collections.singletonList(TemplateType.IF);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
        Map<String, Object> contextMap = featureExecuteContext.toMap();
        log.info("start execute IFExecuteStrategy context={}", JSON.toJSONString(contextMap));
        ExecutorUnit executorUnit = executePoint.getExecutorUnit();
        boolean result;
        String ognl = executorUnit.getService();
        boolean isNewVersion = StringUtils.isNotBlank(ognl);
        if (isNewVersion) {
            result = runCompare(ognl, contextMap);
        } else {
            ognl = executorUnit.getMethod();
            StrSubstitutor strSubstitutor = new StrSubstitutor();
            String replaceOgnl = strSubstitutor.replace(ognl);
            log.info("replace ognl ={}", replaceOgnl);
            result = ognlDataParser.judgeExpression(contextMap, replaceOgnl);
        }
        if (!result) {
            ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
            executeDetailVo.setStatus(true);
            executeDetailVo.setResBody("test condition failed ");
            FeatureResponse featureResponse = FeatureResponse.builder().executeDetailVo(executeDetailVo).build();
            return Collections.singletonList(featureResponse);
        }

        List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
        return executePoints.stream().map(executePointDto -> {
            ExecutePoint point = toExecutePoint(executePointDto);
            return executeFeature(featureExecuteContext, point);
        }).collect(Collectors.toList());
    }
    public boolean runCompare(String replaceOgnl, Map<String, Object> contextMap){
        CompareDefine compareDefine = convertCompareDefine(replaceOgnl, contextMap);
        if (Objects.isNull(compareDefine)) {
            log.info("convert compare define null, compare fail");
            return false;
        }
        //将比较参变量转换成实际值
        log.info("before convert={}",JSON.toJSONString(compareDefine));
        Object object = ognlDataParser.exchangeOgnlValue(contextMap, compareDefine.getCompareKey());
        compareDefine.setResponseValue(object);
        log.info("after convert={}",JSON.toJSONString(compareDefine));

        CompareOperator operator = compareFactory.getOperator(compareDefine.getOperator());
        if (Objects.isNull(operator)) {
            log.info("can not find compare operator={}", compareDefine.getOperator());
            return false;
        }
        CompareResult compareResult = operator.compare(compareDefine);
        boolean compareSuccess = compareResult.isCompareSuccess();
        log.info("compare error message = {}", compareResult.getErrorMessage());
        return compareSuccess;
    }

    /**
     * 此处将[compareKey][operator][propertyKey][expectValue]
     * 转换成CompareDefine
     */
    private static CompareDefine convertCompareDefine(String replaceOgnl, Map<String, Object> contextMap) {
        List<String> strings = exchangeList(replaceOgnl);
        if (CollectionUtils.isEmpty(strings) || strings.size() != 4){
            return null;
        }
        CompareDefine compareDefine = new CompareDefine();
        compareDefine.setCompareKey(strings.get(0));
        compareDefine.setOperator(strings.get(1));
        compareDefine.setResponseValue(contextMap);
        compareDefine.setExpectValue("{" +strings.get(2)+ "}" + strings.get(3));
        return compareDefine;
    }

    private static List<String> exchangeList(String input) {
        String regex = "\\[(.*?)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            String groupValue = matcher.group(1);
            results.add(StringUtils.isBlank(groupValue)? "" : groupValue);
        }
        return results;
    }
}
