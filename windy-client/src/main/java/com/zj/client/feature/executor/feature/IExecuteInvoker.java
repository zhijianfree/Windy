package com.zj.client.feature.executor.feature;


import com.zj.client.feature.executor.feature.invoke.InvokerType;
import com.zj.client.feature.executor.vo.ExecutorUnit;

public interface IExecuteInvoker {

  InvokerType type();

  Object invoke(ExecutorUnit executorUnit);
}
