package com.zj.client.handler.feature.executor.feature.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.ExecutePointDTO;
import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.handler.feature.executor.feature.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Component
public class IFExecuteStrategy extends BaseExecuteStrategy {

  private OgnlDataParser ognlDataParser = new OgnlDataParser();

  public IFExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> executeInvokers,
      CompareHandler compareHandler) {
    super(interceptorProxy, executeInvokers, compareHandler);
  }

  @Override
  public ExecutePointType getType() {
    return ExecutePointType.IF;
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute IFExecuteStrategy");
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit
        .class);
    String ongl = executorUnit.getMethod();
    Boolean result = (Boolean) ognlDataParser.parserExpression(executeContext.toMap(), ongl, null);
    if (!result) {
      return Collections.emptyList();
    }

    List<ExecutePointDTO> executePoints = executorUnit.getExecutePoints();
    return executePoints.stream().map(executePointDTO -> {
      ExecutePoint point = ExecutePointDTO.toExecutePoint(executePointDTO);
      return executeFeature(executeContext, point);
    }).collect(Collectors.toList());
  }
}
