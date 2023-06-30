package com.zj.master.dispatch.listener;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Component
public class TaskInnerEventFactory {
  private Executor executor;
  private static AsyncEventBus eventBus;

  public TaskInnerEventFactory(@Qualifier("eventBusPool") Executor executor) {
    this.executor = executor;
    eventBus = new AsyncEventBus(executor);
  }

  public TaskInnerEventFactory(List<IStopEventListener> notifyList) {
    notifyList.forEach(listener ->{
      eventBus.register(listener);
    });
  }

  public static void sendNotifyEvent(InnerEvent event) {
    eventBus.post(event);
  }
}
