package com.zj.pipeline.executer.notify;

import com.google.common.eventbus.EventBus;
import com.zj.pipeline.executer.IStatusNotifyListener;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEventFactory {

  private static EventBus eventBus = new EventBus();

  public PipelineEventFactory(List<IStatusNotifyListener> notifyList) {
    notifyList.forEach(listener ->{
      eventBus.register(listener);
    });
  }

  public static void sendNotifyEvent(PipelineStatusEvent event) {
    eventBus.post(event);
  }
}
