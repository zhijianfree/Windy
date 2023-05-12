package com.zj.client.feature.executor.feature.strategy;

import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.feature.executor.feature.IExecuteStrategy;
import com.zj.client.feature.executor.vo.ExecuteContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/1/18
 */
@Service
public class ExecuteStrategyFactory {

  private Map<Integer, IExecuteStrategy> executeStrategyMap;

  public ExecuteStrategyFactory(List<IExecuteStrategy> strategies) {
    executeStrategyMap = strategies.stream()
        .collect(Collectors.toMap(strategy -> strategy.getType().getType(), strategy -> strategy));
  }

  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext){
    IExecuteStrategy executeStrategy = executeStrategyMap.get(executePoint.getExecuteType());
    if (Objects.isNull(executeStrategy)) {
      throw new RuntimeException("can not find strategy, not execute");
    }

    return executeStrategy.execute(executePoint, executeContext);
  }
}
