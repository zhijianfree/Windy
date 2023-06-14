package com.zj.client.feature.executor.interceptor;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.ParamDefine;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.client.feature.executor.vo.VariableDefine;
import java.util.List;
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
    String variables = executePoint.getVariables();
    List<VariableDefine> variableDefines = JSON.parseArray(variables, VariableDefine.class);
    if (CollectionUtils.isEmpty(variableDefines)) {
      return;
    }

    //将执行完的结果转化为变量的值
    Object responseBody = executeDetailVo.getResponseDetailVo().getResponseBody();
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
    List<ParamDefine> params = executorUnit.getParams();
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
    });
  }
}
