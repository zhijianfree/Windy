package com.zj.client.handler.pipeline.executer.notify;

import com.google.common.eventbus.EventBus;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 使用eventBus结偶调用
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEventFactory {
  private static final EventBus eventBus = new EventBus();

  public PipelineEventFactory(List<IPipelineStatusListener> notifyList) {
    notifyList.forEach(eventBus::register);
  }

  public static void sendNotifyEvent(PipelineStatusEvent event) {
    eventBus.post(event);
  }
}
