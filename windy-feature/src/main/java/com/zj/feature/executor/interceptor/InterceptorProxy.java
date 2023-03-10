package com.zj.feature.executor.interceptor;

import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;
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

  public void afterExecute(ExecutePoint executePoint, ExecuteDetail executeDetail,
      ExecuteContext context) {
    interceptors.forEach(IExecuteInterceptor -> IExecuteInterceptor
            .afterExecute(executePoint, executeDetail, context));
  }
}
