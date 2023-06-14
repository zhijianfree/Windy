package com.zj.client.feature.executor.interceptor;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InterceptorProxy {

  @Autowired
  private List<IExecuteInterceptor> interceptors;


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
