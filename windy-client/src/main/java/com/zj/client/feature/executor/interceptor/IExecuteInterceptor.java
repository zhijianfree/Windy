package com.zj.client.feature.executor.interceptor;


import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.vo.ExecuteDetail;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecutorUnit;

public interface IExecuteInterceptor {

  void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context);

  void afterExecute(ExecutePoint executePoint, ExecuteDetail executeDetail, ExecuteContext context);

}
