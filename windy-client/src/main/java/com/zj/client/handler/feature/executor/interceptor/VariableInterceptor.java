package com.zj.client.handler.feature.executor.interceptor;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.bo.ExecutePoint;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.invoker.invoke.MethodInvoke;
import com.zj.client.handler.feature.executor.random.IRandomGenerator;
import com.zj.client.handler.feature.executor.random.entity.RandomEntity;
import com.zj.client.handler.feature.executor.random.entity.RandomType;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.feature.VariableDefine;
import com.zj.common.enums.InvokerType;
import com.zj.common.enums.Position;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.ParameterDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VariableInterceptor implements IExecuteInterceptor {
    private static final String VARIABLE_CHAR = "$";
    private final OgnlDataParser ognlDataParser = new OgnlDataParser();

    private final Map<RandomType, IRandomGenerator> randomGeneratorMap;

    public VariableInterceptor(List<IRandomGenerator> randomGenerators) {
        randomGeneratorMap = randomGenerators.stream().collect(Collectors.toMap(IRandomGenerator::randomType,
                generator -> generator));
    }

    @Override
    public void beforeExecute(ExecutorUnit executorUnit, FeatureExecuteContext context) {
        filterVariable(executorUnit, context);
    }

    @Override
    public void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo, FeatureExecuteContext context) {
        //将执行完的结果转化为上下文变量的值
        Object responseBody = executeDetailVo.getResponseDetailVo().getResponseBody();
        exchangeVariableContext(context, executePoint, responseBody);

        //将断言对比的参数设置为具体的值
        exchangeCompareContext(context, executePoint);
    }

    private void exchangeCompareContext(FeatureExecuteContext context, ExecutePoint executePoint) {
        List<CompareDefine> compareDefines = executePoint.getCompareDefines();
        if (CollectionUtils.isEmpty(compareDefines)) {
            return;
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(context.toMap());
        compareDefines.forEach(compareDefine -> {
            String expectValue = compareDefine.getExpectValue();
            if (StringUtils.isBlank(expectValue)) {
                return;
            }

            String replaceResult = strSubstitutor.replace(expectValue);
            compareDefine.setExpectValue(replaceResult);
        });

        executePoint.setCompareDefines(compareDefines);
    }

    private void exchangeVariableContext(FeatureExecuteContext context, ExecutePoint executePoint, Object responseBody) {
        List<VariableDefine> variableDefines = executePoint.getVariableDefines();
        if (CollectionUtils.isEmpty(variableDefines)) {
            return;
        }

        variableDefines.forEach(variableDefine -> {
            String expressionString = variableDefine.getVariableValue();
            if (StringUtils.isBlank(expressionString)) {
                return;
            }

            Object result = ognlDataParser.exchangeOgnlParamValue(responseBody, expressionString);
            context.set(variableDefine.getVariableKey(), result);
            log.info("set context key={} value= {}", variableDefine.getVariableKey(), result);
        });
    }

    /**
     * 执行点在执行之前将变量值替换为全局配置值
     */
    public void filterVariable(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext) {
        List<ParameterDefine> params = executorUnit.getParams();
        if (CollectionUtils.isNotEmpty(params)) {
            filterParam(featureExecuteContext, params);
        }

        //如果是HTTP请求的方式，还需要给service(url)、header替换变量参数
        if (Objects.equals(executorUnit.getInvokeType(), InvokerType.HTTP.getType()) ||
                Objects.equals(executorUnit.getInvokeType(), InvokerType.RELATED_TEMPLATE.getType())) {
            filterHttpInvokerParam(executorUnit, featureExecuteContext);
        }

        //将关联模版的参数替换变量
        if (Objects.nonNull(executorUnit.getRelatedTemplate())) {
            filterVariable(executorUnit.getRelatedTemplate(), featureExecuteContext);
        }
    }

    private void filterHttpInvokerParam(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext) {
        //此处使用新的Map是避免ExecuteContext被局部参数污染
        Map<String, Object> pointContext = new HashMap<>(featureExecuteContext.toMap());
        //service(url)中的存在路径参数，所以需要将路径参数的值替换
        if (CollectionUtils.isNotEmpty(executorUnit.getParams())) {
            Map<String, Object> pointParams = executorUnit.getParams().stream()
                    .map(p -> {
                        //如果是Query参数value为null时，那么就将value设置为空字符串避免污染url
                        //比如:http://192.168.1.1:8000/test?name=${name}，如果变量name为null执行之前应该转化为
                        //新的url:http://192.168.1.1:8000/test?name=
                        if (Objects.equals(p.getPosition(), Position.Query.name()) && Objects.isNull(p.getValue())) {
                            p.setValue("");
                        }
                        return p;
                    })
                    .filter(p -> Objects.nonNull(p.getValue()) && !String.valueOf(p.getValue()).contains(VARIABLE_CHAR))
                    .collect(Collectors.toMap(ParameterDefine::getParamKey, ParameterDefine::getValue));
            pointContext.putAll(pointParams);
        }

        StrSubstitutor pointSubstitutor = new StrSubstitutor(pointContext);
        String replaceResult = pointSubstitutor.replace(executorUnit.getService());
        executorUnit.setService(replaceResult);

        if (MapUtils.isNotEmpty(executorUnit.getHeaders())) {
            Map<String, String> exchangeHeaders =
                    executorUnit.getHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> pointSubstitutor.replace(entry.getValue())));
            log.info("replace headers={}", exchangeHeaders);
            executorUnit.setHeaders(exchangeHeaders);
        }
    }

    private void filterParam(FeatureExecuteContext featureExecuteContext, List<ParameterDefine> params) {
        //如果执行点的参数使用了环境变量则需要转换变量
        StrSubstitutor strSubstitutor = new StrSubstitutor(featureExecuteContext.toMap());
        params.forEach(param -> {
            Object paramValue = getParamValueWithDefaultValue(param);
            if (!String.valueOf(paramValue).contains(VARIABLE_CHAR)){
                return;
            }

            Object randomValue = generateRandomRule(paramValue);
            if (Objects.nonNull(randomValue)) {
                param.setValue(randomValue);
                return;
            }

             if (Objects.equals(param.getType(), ParamValueType.Map.name())) {
                Map<String, String> map = (Map<String, String>) paramValue;
                Map<String, String> result = map.keySet().stream().collect(Collectors.toMap(strSubstitutor::replace,
                        key -> strSubstitutor.replace(map.get(key))));
                param.setValue(result);
            } else if (Objects.equals(param.getType(), ParamValueType.Array.name())) {
                 String arrayString = JSON.toJSONString(paramValue);
                 String replaceResult = strSubstitutor.replace(arrayString);
                 List<Object> objects = JSON.parseArray(replaceResult, Object.class);
                 param.setValue(objects);
             } else if (Objects.equals(param.getType(), ParamValueType.Object.name())) {
                 String arrayString = JSON.toJSONString(paramValue);
                 String replaceResult = strSubstitutor.replace(arrayString);
                 Object object = JSON.parseObject(replaceResult, Object.class);
                 param.setValue(object);
             }else {
                String stringValue = String.valueOf(paramValue);
                String replaceResult = strSubstitutor.replace(stringValue);
                param.setValue(replaceResult);
                Object value = MethodInvoke.convertDataToType(param);
                param.setValue(value);
            }
            log.info("convert param key={} type={} convertResult={}", param.getParamKey(), param.getType(),
                    param.getValue());
        });
    }

    private Object generateRandomRule(Object paramValue) {
        RandomEntity randomEntity = RandomType.exchangeRandomType(String.valueOf(paramValue));
        if (Objects.isNull(randomEntity)) {
            return null;
        }

        IRandomGenerator generator = randomGeneratorMap.get(randomEntity.getRandomType());
        if (Objects.isNull(generator)) {
            return null;
        }
        return generator.generateRandom(randomEntity.getRandomRule());
    }

    private Object getParamValueWithDefaultValue(ParameterDefine param) {
        return Optional.ofNullable(param.getValue()).orElseGet(() -> {
            if (Objects.isNull(param.getInitData())) {
                return null;
            }
            return param.getInitData().getData();
        });
    }
}
