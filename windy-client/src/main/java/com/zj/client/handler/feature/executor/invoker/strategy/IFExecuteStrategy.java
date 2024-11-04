package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.TemplateType;
import com.zj.common.entity.feature.ExecutePointDto;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Component
public class IFExecuteStrategy extends BaseExecuteStrategy {

  private final OgnlDataParser ognlDataParser = new OgnlDataParser();

  public IFExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> executeInvokers,
      CompareHandler compareHandler) {
    super(interceptorProxy, executeInvokers, compareHandler);
  }

  @Override
  public List<TemplateType> getType() {
    return Collections.singletonList(TemplateType.IF);
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
    log.info("start execute IFExecuteStrategy context={}", JSON.toJSONString(featureExecuteContext.toMap()));
    ExecutorUnit executorUnit = executePoint.getExecutorUnit();
    String ognl = executorUnit.getMethod();
    StrSubstitutor strSubstitutor = new StrSubstitutor(featureExecuteContext.toMap());
    String replaceOgnl = strSubstitutor.replace(ognl);
    log.info("replace ognl ={}", replaceOgnl);
    boolean result = ognlDataParser.judgeExpression(featureExecuteContext.toMap(), replaceOgnl);
    if (!result) {
      ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
      executeDetailVo.setStatus(true);
      executeDetailVo.addRequestInfo("校验条件", executorUnit.getMethod());
      executeDetailVo.setResBody("校验不通过");
      FeatureResponse featureResponse = FeatureResponse.builder().executeDetailVo(executeDetailVo).build();
      return Collections.singletonList(featureResponse);
    }

    List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
    return executePoints.stream().map(executePointDto -> {
      ExecutePoint point = toExecutePoint(executePointDto);
      return executeFeature(featureExecuteContext, point);
    }).collect(Collectors.toList());
  }
}
