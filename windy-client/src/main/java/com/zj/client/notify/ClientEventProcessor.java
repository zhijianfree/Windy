package com.zj.client.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.ResultEvent;
import com.zj.common.monitor.InstanceMonitor;
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
  private final RestTemplate restTemplate;
  private final InstanceMonitor instanceMonitor;
  private final LocalPersistence localPersistence;
  public static final String WINDY_MASTER = "WindyMaster";
  public static final String NOTIFY_MASTER_URL = "http://WindyMaster/v1/devops/dispatch/notify";
  private final MediaType mediaType = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10,
      TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();

  public ClientEventProcessor(DiscoveryClient discoveryClient, RestTemplate restTemplate,
      InstanceMonitor instanceMonitor, LocalPersistence localPersistence) {
    this.discoveryClient = discoveryClient;
    this.restTemplate = restTemplate;
    this.instanceMonitor = instanceMonitor;
    this.localPersistence = localPersistence;
  }

  @Override
  public boolean notifyEvent(ResultEvent resultEvent) {
    log.info("start notify result={} ", JSON.toJSONString(resultEvent));
    try {
      List<ServiceInstance> windyMaster = discoveryClient.getInstances(WINDY_MASTER);
      Optional<ServiceInstance> optional = windyMaster.stream().filter(
              serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
          .findFirst();
      if (optional.isPresent()) {
        // 如果触发任务执行的master节点存在那么优先访问触发任务的master节点
        return notifyWithMasterIP(resultEvent, optional.get());
      }

      //master节点不可达时，尝试使用其他的master节点
      HttpEntity<ResultEvent> httpEntity = new HttpEntity<>(resultEvent);
      ResponseEntity<String> response = restTemplate.postForEntity(NOTIFY_MASTER_URL, httpEntity,
          String.class);
      log.info("notify event code={} result={}", response.getStatusCode(), response.getBody());
      return response.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("notify event error save to file", e);
      localPersistence.persistNotify(resultEvent);
    }
    return false;
  }

  private boolean notifyWithMasterIP(ResultEvent resultEvent, ServiceInstance serviceInstance) {
    String masterHost = serviceInstance.getHost() + ":" + serviceInstance.getPort();
    String url = NOTIFY_MASTER_URL.replace(WINDY_MASTER, masterHost);
    Request request = new Request.Builder().url(url)
        .post(RequestBody.create(mediaType, JSON.toJSONString(resultEvent))).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      log.info("notify master ip status result code={} result={}", response.code(),
          response.body().string());
      return response.isSuccessful();
    } catch (Exception e) {
      log.error("request master ip error", e);
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
