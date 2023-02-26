package com.zj.feature.executor.interceptor;

import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;

public interface IExecuteInterceptor {

  void beforeExecute(ExecutorUnit executorUnit, ExecuteContext context);

  void afterExecute(ExecutePoint executePoint, ExecuteDetail executeDetail, ExecuteContext context);

}
