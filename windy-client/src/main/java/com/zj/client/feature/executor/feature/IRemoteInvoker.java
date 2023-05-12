package com.zj.client.feature.executor.feature;


import com.zj.client.feature.executor.vo.ExecutorUnit;

public interface IRemoteInvoker {

  Object invoke(ExecutorUnit executorUnit);
}
