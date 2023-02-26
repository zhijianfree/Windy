package com.zj.feature.executor.feature.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.feature.entity.dto.ExecutePointDTO;
import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.type.ExecutePointType;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.compare.CompareHandler;
import com.zj.feature.executor.interceptor.InterceptorProxy;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/1/17
 */
@Slf4j
@Component
public class ForExecuteStrategy extends BaseExecuteStrategy{

  public ForExecuteStrategy(InterceptorProxy interceptorProxy,
      com.zj.feature.executor.feature.IRemoteInvoker IRemoteInvoker,
      CompareHandler compareHandler) {
    super(interceptorProxy, IRemoteInvoker, compareHandler);
  }

  @Override
  public ExecutePointType getType() {
    return ExecutePointType.FOR;
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute ForExecuteStrategy");
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit
        .class);
    List<FeatureResponse> responses = new ArrayList<>();
    List<ExecutePointDTO> executePoints = executorUnit.getExecutePoints();
    int size = Integer.parseInt(executorUnit.getMethod());
    for (int i = 0; i < size; i++) {
      executeContext.set("$item", i);
      List<FeatureResponse> responseList = executePoints.stream().map(executePointDTO -> {
        ExecutePoint point = ExecutePointDTO.toExecutePoint(executePointDTO);
        return executeFeature(executeContext, point);
      }).collect(Collectors.toList());

      executeContext.remove("$item");
      responses.addAll(responseList);
    }
    return responses;
  }
}
