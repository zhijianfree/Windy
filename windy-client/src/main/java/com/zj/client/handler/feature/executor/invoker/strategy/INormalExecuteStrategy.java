package com.zj.client.handler.feature.executor.invoker.strategy;

import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Service
public class INormalExecuteStrategy extends BaseExecuteStrategy {

    public INormalExecuteStrategy(InterceptorProxy interceptorProxy,
                                  List<IExecuteInvoker> executeInvokers,
                                  CompareHandler compareHandler) {
        super(interceptorProxy, executeInvokers, compareHandler);
    }

    @Override
    public List<ExecutePointType> getType() {
        return Arrays.asList(ExecutePointType.NORMAL, ExecutePointType.DEFAULT);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
        log.info("start execute INormalExecuteStrategy pointId={}", executePoint.getPointId());
        FeatureResponse featureResponse = executeFeature(executeContext, executePoint);
        return Collections.singletonList(featureResponse);
    }
}
