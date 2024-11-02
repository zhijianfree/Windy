package com.zj.client.handler.notify;


import com.zj.common.entity.dto.ResultEvent;

public interface IResultEventNotify {

  /**
   * 节点或者用例执行完成之后执行
   */
  boolean notifyEvent(ResultEvent resultEvent);

}
