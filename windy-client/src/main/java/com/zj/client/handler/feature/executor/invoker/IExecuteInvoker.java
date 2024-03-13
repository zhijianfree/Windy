package com.zj.client.handler.feature.executor.invoker;


import com.zj.common.enums.InvokerType;
import com.zj.common.feature.ExecutorUnit;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit);
}
