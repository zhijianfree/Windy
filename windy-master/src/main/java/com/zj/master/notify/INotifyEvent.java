package com.zj.master.notify;

import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface INotifyEvent {

  NotifyType type();

  boolean handle(ResultEvent resultEvent);
}
