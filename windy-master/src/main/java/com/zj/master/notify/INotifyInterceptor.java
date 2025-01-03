package com.zj.master.notify;

import com.zj.common.entity.dto.ResultEvent;

public interface INotifyInterceptor {

    /**
     * 事件通知之前处理
     * @param resultEvent 通知事件
     */
    default void before(ResultEvent resultEvent){}

    /**
     * 事件通知之后处理
     * @param handleResult 通知事件处理结果
     * @param resultEvent 通知事件
     */
    default void after(ResultEvent resultEvent, boolean handleResult){}
}
