package com.zj.client.handler.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.ResultEvent;
import com.zj.common.monitor.InstanceMonitor;
import com.zj.common.monitor.RequestProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Slf4j
@Component
public class ClientEventProcessor implements IResultEventNotify {

  private final RequestProxy requestProxy;
  private final InstanceMonitor instanceMonitor;
  private final OptimizePersistLocal optimizePersistLocal;

  public ClientEventProcessor(RequestProxy requestProxy, InstanceMonitor instanceMonitor,
      OptimizePersistLocal optimizePersistLocal) {
    this.requestProxy = requestProxy;
    this.instanceMonitor = instanceMonitor;
    this.optimizePersistLocal = optimizePersistLocal;
  }

  @Override
  public boolean notifyEvent(ResultEvent resultEvent) {
    log.info("start notify client event={} executeId={}", resultEvent.getExecuteType(),
        resultEvent.getExecuteId());
    try {
      return requestProxy.clientNotifyEvent(resultEvent);
    } catch (Exception e) {
      log.error("notify event error save to file", e);
      optimizePersistLocal.persistNotify(resultEvent);
    }
    return false;
  }

  /**
   * 定时将未通知成功的状态同步到master
   */
  @Scheduled(cron = "0/10 * * * * ? ")
  public void asyncNotifyPersist() {
    if (instanceMonitor.isUnStable()) {
      return;
    }

    handleNotifyFromCache();
    handleNotifyFromFile();
  }

  private void handleNotifyFromCache() {
    List<ResultEvent> resultEvents = optimizePersistLocal.getCacheList();
    List<ResultEvent> handledEvents = resultEvents.stream().filter(this::notifyEvent)
        .collect(Collectors.toList());
    optimizePersistLocal.removeCache(handledEvents);
  }

  private void handleNotifyFromFile() {
    List<ResultEvent> resultEvents = optimizePersistLocal.readEventsFromFile();
    if (CollectionUtils.isEmpty(resultEvents)) {
      return;
    }

    //从文件读取到所有的数据后直接全部清清除
    optimizePersistLocal.clearFileContent();
    resultEvents.forEach(this::notifyEvent);
  }
}
