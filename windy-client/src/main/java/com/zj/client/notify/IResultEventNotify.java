package com.zj.client.notify;


public interface IResultEventNotify {

  /**
   * 节点或者用例执行完成之后
   */
  void notifyEvent(ResultEvent resultEvent);

}
