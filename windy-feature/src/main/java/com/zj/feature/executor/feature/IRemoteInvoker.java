package com.zj.feature.executor.feature;

import com.zj.feature.executor.vo.ExecutorUnit;

public interface IRemoteInvoker {

  Object invoke(ExecutorUnit executorUnit);
}
