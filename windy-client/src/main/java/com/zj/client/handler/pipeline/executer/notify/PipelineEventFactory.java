package com.zj.client.handler.pipeline.executer.notify;

import com.google.common.eventbus.EventBus;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 使用eventBus结偶调用
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEventFactory {
  private static EventBus eventBus = new EventBus();

  public PipelineEventFactory(List<IPipelineStatusListener> notifyList) {
    notifyList.forEach(listener ->{
      eventBus.register(listener);
    });
  }

  public static void sendNotifyEvent(PipelineStatusEvent event) {
    eventBus.post(event);
  }
}
