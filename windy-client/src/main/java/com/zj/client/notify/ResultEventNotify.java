package com.zj.client.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.ResultEvent;
import com.zj.common.monitor.InstanceMonitor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
public class ResultEventNotify implements IResultEventNotify {

  @Autowired
  private DiscoveryClient discoveryClient;
  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private InstanceMonitor instanceMonitor;

  @Autowired
  private LocalPersistence localPersistence;

  public static final String WINDY_MASTER = "WindyMaster";
  public static final String NOTIFY_MASTER_URL = "http://WindyMaster/v1/devops/dispatch/notify";
  private final MediaType mediaType = MediaType.get("application/json; charset=utf-8");
  private final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10,
      TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();

  @Override
  public void notifyEvent(ResultEvent resultEvent) {
    log.info("start notify result={} ", JSON.toJSONString(resultEvent));
    try {
      List<ServiceInstance> windyMaster = discoveryClient.getInstances(WINDY_MASTER);
      Optional<ServiceInstance> optional = windyMaster.stream().filter(
              serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
          .findFirst();
      if (optional.isPresent()) {
        // 如果触发任务执行的master节点存在那么优先访问触发任务的master节点
        notifyWithMasterIP(resultEvent, optional.get());
        return;
      }

      //master节点不可达时，尝试使用其他的master节点
      HttpEntity<ResultEvent> httpEntity = new HttpEntity<>(resultEvent);
      ResponseEntity<String> response = restTemplate.postForEntity(NOTIFY_MASTER_URL, httpEntity,
          String.class);
      log.info("notify event code={} result={}", response.getStatusCode(), response.getBody());
    } catch (Exception e) {
      log.error("notify event error save to file", e);
      localPersistence.persistNotify(resultEvent);
    }
  }

  private void notifyWithMasterIP(ResultEvent resultEvent, ServiceInstance serviceInstance) {
    String masterHost = serviceInstance.getHost() + ":" + serviceInstance.getPort();
    String url = NOTIFY_MASTER_URL.replace(WINDY_MASTER, masterHost);
    Request request = new Request.Builder().url(url)
        .post(RequestBody.create(mediaType, JSON.toJSONString(resultEvent))).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      log.info("notify master ip status result code={} result={}", response.code(),
          response.body().string());
    } catch (Exception e) {
      log.error("request master ip error", e);
    }
  }

  @Scheduled(cron = "0/5 * * * * ? ")
  public void asyncNotifyPersist() {
    if (!instanceMonitor.isSuitable()){
      return;
    }

    List<ResultEvent> resultEvents = localPersistence.readEventsFromFile();
    if (CollectionUtils.isEmpty(resultEvents)) {
      return;
    }
    resultEvents.forEach(this::notifyEvent);
    localPersistence.clearFileContent();
  }
}
