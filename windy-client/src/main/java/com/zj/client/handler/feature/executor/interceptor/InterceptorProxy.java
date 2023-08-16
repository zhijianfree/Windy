package com.zj.client.handler.feature.executor.interceptor;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InterceptorProxy {

  private List<IExecuteInterceptor> interceptors;

  public InterceptorProxy(List<IExecuteInterceptor> interceptors) {
    this.interceptors = interceptors;
  }


  public void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context) {
    interceptors.forEach(IExecuteInterceptor ->
        IExecuteInterceptor.beforeExecute(executorUnit, context));
  }

  public void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo,
      ExecuteContext context) {
    interceptors.forEach(IExecuteInterceptor -> IExecuteInterceptor
            .afterExecute(executePoint, executeDetailVo, context));
  }
}
