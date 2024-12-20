package com.zj.client.handler.feature.executor.invoker;


import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.InvokerType;
import com.zj.common.entity.feature.ExecutorUnit;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext);
}
