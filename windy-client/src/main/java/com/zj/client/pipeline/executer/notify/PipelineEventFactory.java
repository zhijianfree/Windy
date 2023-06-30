package com.zj.client.pipeline.executer.notify;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.zj.client.pipeline.executer.vo.PipelineStatusEvent;
import java.util.List;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 使用eventBus结偶调用
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEventFactory {

  private Executor executor;
  private static AsyncEventBus eventBus;

  public PipelineEventFactory(@Qualifier("eventBusPool") Executor executor) {
    this.executor = executor;
    eventBus = new AsyncEventBus(executor);
  }

  public PipelineEventFactory(List<IPipelineStatusListener> notifyList) {
    notifyList.forEach(listener ->{
      eventBus.register(listener);
    });
  }

  public static void sendNotifyEvent(PipelineStatusEvent event) {
    eventBus.post(event);
  }
}
