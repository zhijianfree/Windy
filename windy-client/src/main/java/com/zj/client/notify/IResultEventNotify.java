package com.zj.client.notify;

import com.zj.common.enums.ProcessStatus;

public interface IResultEventNotify {

  /**
   * 节点或者用例执行完成之后
   */
  public void notify(String executeId, NotifyType notifyType, ProcessStatus status, Object object);

}
