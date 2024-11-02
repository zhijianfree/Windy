package com.zj.client.handler.feature.executor.invoker.strategy;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.invoker.IExecuteStrategy;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.exception.ExecuteException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/1/18
 */
@Service
public class ExecuteStrategyFactory {

    private final Map<Integer, IExecuteStrategy> executeStrategyMap = new HashMap<>();

    public ExecuteStrategyFactory(List<IExecuteStrategy> strategies) {
        strategies.forEach(strategy -> strategy.getType().forEach(type -> executeStrategyMap.put(type.getType(),
                strategy)));
    }

    public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
        IExecuteStrategy executeStrategy = executeStrategyMap.get(executePoint.getExecuteType());
        if (Objects.isNull(executeStrategy)) {
            throw new ExecuteException("can not find strategy, not execute");
        }
        return executeStrategy.execute(executePoint, featureExecuteContext);
    }
}
