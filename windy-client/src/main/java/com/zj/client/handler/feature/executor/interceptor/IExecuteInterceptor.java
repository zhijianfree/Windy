package com.zj.client.handler.feature.executor.interceptor;


import com.zj.client.entity.bo.ExecutePoint;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.entity.feature.ExecutorUnit;

public interface IExecuteInterceptor {

  void beforeExecute(ExecutorUnit executorUnit, FeatureExecuteContext context);

  void afterExecute(ExecutePoint executePoint, ExecuteDetailVo executeDetailVo, FeatureExecuteContext context);

}
