package com.zj.client.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.ResultEvent;
import com.zj.common.monitor.InstanceMonitor;
import com.zj.common.monitor.RequestProxy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Slf4j
@Component
public class ClientEventProcessor implements IResultEventNotify {
  private final DiscoveryClient discoveryClient;
  private final RequestProxy requestProxy;
  private final InstanceMonitor instanceMonitor;
  private final LocalPersistence localPersistence;

  public ClientEventProcessor(DiscoveryClient discoveryClient, RequestProxy requestProxy,
      InstanceMonitor instanceMonitor, LocalPersistence localPersistence) {
    this.discoveryClient = discoveryClient;
    this.requestProxy = requestProxy;
    this.instanceMonitor = instanceMonitor;
    this.localPersistence = localPersistence;
  }

  @Override
  public boolean notifyEvent(ResultEvent resultEvent) {
    log.info("start notify result={} ", JSON.toJSONString(resultEvent));
    try {
      return requestProxy.clientNotifyEvent(resultEvent);
    } catch (Exception e) {
      log.error("notify event error save to file", e);
      localPersistence.persistNotify(resultEvent);
    }
    return false;
  }

  /**
   * 定时将未通知成功的状态同步到master
   * */
  @Scheduled(cron = "0/10 * * * * ? ")
  public void asyncNotifyPersist() {
    if (!instanceMonitor.isSuitable()){
      return;
    }

    handleNotifyFromCache();
    handleNotifyFromFile();
  }

  private void handleNotifyFromCache() {
    List<ResultEvent> resultEvents = localPersistence.getCacheList();
    List<ResultEvent> handledEvents = resultEvents.stream().filter(this::notifyEvent)
        .collect(Collectors.toList());
    localPersistence.removeCache(handledEvents);
  }

  private void handleNotifyFromFile() {
    List<ResultEvent> resultEvents = localPersistence.readEventsFromFile();
    if (CollectionUtils.isEmpty(resultEvents)) {
      return;
    }
    localPersistence.clearFileContent();
    resultEvents.forEach(this::notifyEvent);
  }
}
