package com.zj.client.handler.feature.executor.interceptor;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.entity.feature.ExecutorUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InterceptorProxy {

  private List<IExecuteInterceptor> interceptors;

  public InterceptorProxy(List<IExecuteInterceptor> interceptors) {
    this.interceptors = interceptors;
  }


  public void beforeExecute(ExecutorUnit executorUnit, FeatureExecuteContext context) {
    interceptors.forEach(executeInterceptor ->
            executeInterceptor.beforeExecute(executorUnit, context));
  }

  public void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo,
      FeatureExecuteContext context) {
    interceptors.forEach(executeInterceptor -> executeInterceptor
            .afterExecute(executePoint, executeDetailVo, context));
  }
}
