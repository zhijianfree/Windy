package com.zj.client.handler.feature.executor.invoker;


import com.zj.common.enums.InvokerType;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit);
}
