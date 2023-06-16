package com.zj.master.dispatch.listener;

import com.google.common.eventbus.EventBus;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Component
public class TaskInnerEventFactory {
  private static EventBus eventBus = new EventBus();

  public TaskInnerEventFactory(List<IStopEventListener> notifyList) {
    notifyList.forEach(listener ->{
      eventBus.register(listener);
    });
  }

  public static void sendNotifyEvent(InnerEvent event) {
    eventBus.post(event);
  }
}
