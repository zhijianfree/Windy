package com.zj.client.handler.feature.executor.invoker.strategy;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.TemplateType;
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
    public List<TemplateType> getType() {
        return Arrays.asList(TemplateType.NORMAL, TemplateType.DEFAULT, TemplateType.PLUGIN);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
        log.info("start execute INormalExecuteStrategy pointId={}", executePoint.getPointId());
        FeatureResponse featureResponse = executeFeature(featureExecuteContext, executePoint);
        return Collections.singletonList(featureResponse);
    }
}
