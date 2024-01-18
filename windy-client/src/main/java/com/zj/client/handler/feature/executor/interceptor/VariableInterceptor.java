package com.zj.client.handler.feature.executor.interceptor;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;
import com.zj.client.handler.feature.executor.vo.VariableDefine;
import com.zj.plugin.loader.ParameterDefine;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class VariableInterceptor implements IExecuteInterceptor {

  private static final String VARIABLE_CHAR = "$";
  private OgnlDataParser ognlDataParser = new OgnlDataParser();

  @Override
  public void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context) {
    filterVariable(executorUnit, context);
  }

  @Override
  public void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo,
      ExecuteContext context) {
    //将执行完的结果转化为上下文变量的值
    Object responseBody = executeDetailVo.getResponseDetailVo().getResponseBody();
    exchangeVariableContext(context, executePoint, responseBody);

    //将断言对比的参数设置为具体的值
    exchangeCompareContext(context, executePoint);
  }

  private void exchangeCompareContext(ExecuteContext context, ExecutePoint executePoint) {
    List<CompareDefine> compareDefines = JSON.parseArray(executePoint.getCompareDefine(), CompareDefine.class);
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

    executePoint.setCompareDefine(JSON.toJSONString(compareDefines));
  }

  private void exchangeVariableContext(ExecuteContext context, ExecutePoint executePoint, Object responseBody) {
    List<VariableDefine> variableDefines = JSON.parseArray(executePoint.getVariables(), VariableDefine.class);
    if (CollectionUtils.isEmpty(variableDefines)) {
      return;
    }

    variableDefines.forEach(variableDefine -> {
      String expressionString = variableDefine.getVariableValue();
      if (StringUtils.isBlank(expressionString)) {
        return;
      }

      //字符串替换 $ => #
      if (expressionString.startsWith(VARIABLE_CHAR)) {
        expressionString = expressionString.replace(VARIABLE_CHAR, "#");
      }

      Object result = ognlDataParser.parserExpression(responseBody, expressionString);
      context.set(variableDefine.getVariableKey(), result);
    });
  }

  /**
   * 执行点在执行之前将变量值替换为全局配置值
   */
  private void filterVariable(ExecutorUnit executorUnit, ExecuteContext executeContext) {
    List<ParameterDefine> params = executorUnit.getParams();
    if (CollectionUtils.isEmpty(params)) {
      return;
    }

    //如果执行点的参数使用了环境变量则需要转换变量
    StrSubstitutor strSubstitutor = new StrSubstitutor(executeContext.toMap());
    params.forEach(param -> {
      Object paramValue = param.getValue();
      if (paramValue instanceof String) {
        String stringValue = String.valueOf(paramValue);
        String replaceResult = strSubstitutor.replace(stringValue);
        param.setValue(replaceResult);
      }

      if (paramValue instanceof Map) {
        Map<String, String> map = (Map<String, String>) paramValue;
        Map<String, String> result = map.keySet().stream().collect(
            Collectors.toMap(strSubstitutor::replace,
                key -> strSubstitutor.replace(map.get(key))));
        param.setValue(result);
      }
    });
  }
}
