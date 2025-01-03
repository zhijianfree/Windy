package com.zj.master.dispatch.listener;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.EventBus;
import java.util.List;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class InternalEventFactory {
  private static final EventBus eventBus = new EventBus(new EventBusExceptionListener());

  public InternalEventFactory(List<IStopEventListener> notifyList) {
    notifyList.forEach(eventBus::register);
  }

  public static void sendNotifyEvent(InternalEvent event) {
    eventBus.post(event);
  }


  public static class  EventBusExceptionListener implements SubscriberExceptionHandler {
    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
      log.info("catch event bus exception event={}", JSON.toJSONString(context.getEvent()), exception);
    }
  }
}
