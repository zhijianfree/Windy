package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.handler.feature.executor.compare.ognl.DefaultMemberAccess;
import com.zj.common.feature.ExecutePointDto;
import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.common.feature.ExecutorUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.springframework.stereotype.Component;

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
  public List<ExecutePointType> getType() {
    return Collections.singletonList(ExecutePointType.IF);
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute IFExecuteStrategy context={}", JSON.toJSONString(executeContext.toMap()));
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit
        .class);
    String ongl = executorUnit.getMethod();
    boolean result = ognlDataParser.parserExpression(executeContext.toMap(), ongl);
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
      return executeFeature(executeContext, point);
    }).collect(Collectors.toList());
  }
}
