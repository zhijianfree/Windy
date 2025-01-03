package com.zj.client.handler.feature.executor.invoker.strategy;

import com.zj.client.entity.bo.ExecutePoint;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.TemplateType;
import com.zj.common.entity.feature.ExecutePointDto;
import com.zj.common.entity.feature.ExecutorUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Component
public class ForExecuteStrategy extends BaseExecuteStrategy{

  public ForExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> executeInvokers,
      CompareHandler compareHandler) {
    super(interceptorProxy, executeInvokers, compareHandler);
  }

  @Override
  public List<TemplateType> getType() {
    return Collections.singletonList(TemplateType.FOR);
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
    log.info("start execute ForExecuteStrategy");
    ExecutorUnit executorUnit = executePoint.getExecutorUnit();
    List<FeatureResponse> responses = new ArrayList<>();
    List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
    int size = Integer.parseInt(executorUnit.getMethod());
    for (int i = 0; i < size; i++) {
      featureExecuteContext.set("$index", i);
      List<FeatureResponse> responseList = executePoints.stream().map(executePointDto -> {
        ExecutePoint point = toExecutePoint(executePointDto);
        return executeFeature(featureExecuteContext, point);
      }).collect(Collectors.toList());

      featureExecuteContext.remove("$index");
      responses.addAll(responseList);
    }
    return responses;
  }
}
