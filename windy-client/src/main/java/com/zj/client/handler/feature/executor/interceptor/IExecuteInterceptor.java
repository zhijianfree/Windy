package com.zj.client.handler.feature.executor.interceptor;


import com.zj.client.entity.vo.ExecutePoint;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.common.feature.ExecutorUnit;

public interface IExecuteInterceptor {

  void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context);

  void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo, ExecuteContext context);

}
