package com.zj.client.feature.executor.feature.strategy;

import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.feature.executor.compare.CompareHandler;
import com.zj.client.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.feature.executor.vo.ExecuteContext;
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
      com.zj.client.feature.executor.feature.IRemoteInvoker IRemoteInvoker,
      CompareHandler compareHandler) {
    super(interceptorProxy, IRemoteInvoker, compareHandler);
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
