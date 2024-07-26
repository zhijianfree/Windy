package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.feature.ExecutePointDto;
import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.common.feature.ExecutorUnit;
import java.util.ArrayList;
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
public class ForExecuteStrategy extends BaseExecuteStrategy{

  public ForExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> executeInvokers,
      CompareHandler compareHandler) {
    super(interceptorProxy, executeInvokers, compareHandler);
  }

  @Override
  public List<ExecutePointType> getType() {
    return Collections.singletonList(ExecutePointType.FOR);
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute ForExecuteStrategy");
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit
        .class);
    List<FeatureResponse> responses = new ArrayList<>();
    List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
    int size = Integer.parseInt(executorUnit.getMethod());
    for (int i = 0; i < size; i++) {
      executeContext.set("$item", i);
      List<FeatureResponse> responseList = executePoints.stream().map(executePointDto -> {
        ExecutePoint point = toExecutePoint(executePointDto);
        return executeFeature(executeContext, point);
      }).collect(Collectors.toList());

      executeContext.remove("$item");
      responses.addAll(responseList);
    }
    return responses;
  }
}
