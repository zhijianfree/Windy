package com.zj.client.handler.feature.executor.feature.strategy;

import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.feature.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
@Service
public class INormalExecuteStrategy extends BaseExecuteStrategy {

  public INormalExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> IExecuteInvokers,
      CompareHandler compareHandler) {
    super(interceptorProxy, IExecuteInvokers, compareHandler);
  }

  @Override
  public ExecutePointType getType() {
    return ExecutePointType.NORMAL;
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute INormalExecuteStrategy");
    FeatureResponse featureResponse = executeFeature(executeContext, executePoint);
    return Collections.singletonList(featureResponse);
  }
}
