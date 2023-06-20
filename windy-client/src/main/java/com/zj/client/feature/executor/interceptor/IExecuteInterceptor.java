package com.zj.client.feature.executor.interceptor;


import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecutorUnit;

public interface IExecuteInterceptor {

  void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context);

  void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo, ExecuteContext context);

}