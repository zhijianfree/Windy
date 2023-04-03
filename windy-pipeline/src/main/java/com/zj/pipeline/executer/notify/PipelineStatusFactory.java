package com.zj.pipeline.executer.notify;

import com.zj.pipeline.executer.IStatusNotifyListener;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineStatusFactory {

  private final List<IStatusNotifyListener> notifyList;

  public PipelineStatusFactory(List<IStatusNotifyListener> notifyList) {
    this.notifyList = notifyList;
  }

  public void sendNotifyEvent(PipelineStatusEvent event) {
    if (CollectionUtils.isEmpty(notifyList)) {
      return;
    }

    notifyList.forEach(statusNotify -> {
      try {
        statusNotify.statusChange(event);
      } catch (Exception e) {
        log.error("notify error", e);
      }
    });
  }
}
