package com.zj.feature.executor.feature.strategy;

import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.feature.entity.type.ExecutePointType;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.compare.CompareHandler;
import com.zj.feature.executor.interceptor.InterceptorProxy;
import com.zj.feature.executor.vo.ExecuteContext;
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
public class INormalExecuteStrategy extends BaseExecuteStrategy{

  public INormalExecuteStrategy(InterceptorProxy interceptorProxy,
      com.zj.feature.executor.feature.IRemoteInvoker IRemoteInvoker,
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
