package com.zj.master.dispatch;

import com.alibaba.fastjson.JSON;
import com.zj.master.discover.DiscoverService;
import com.zj.master.entity.vo.BaseDispatch;
import com.zj.common.model.StopDispatch;
import com.zj.master.entity.vo.ServiceInstance;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Component
public class ClientProxy {
  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private DiscoverService discoverService;
  public static final String DISPATCH_TASK_CLIENT_URL = "http://WindyClient/v1/client/task";
  public static final String STOP_TASK_CLIENT_URL = "http://%s/v1/client/task/stop";
  private final OkHttpClient okHttpClient = new OkHttpClient();
  private final okhttp3.MediaType mediaType = okhttp3.MediaType.get("application/json; charset=utf-8");

  public boolean sendDispatchTask(BaseDispatch baseDispatch) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<BaseDispatch> http = new HttpEntity<>(baseDispatch, headers);
    log.info("request body={}", JSON.toJSONString(baseDispatch));
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(DISPATCH_TASK_CLIENT_URL,
          http, String.class);
      log.info("get response status result ={}", responseEntity.getBody());
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.info("send dispatch task error", e);
    }
    return false;
  }

  /**
   * 此处选择通知所有client节点
   */
  public void stopDispatchTask(StopDispatch stopDispatch) {
    CompletableFuture.runAsync(() -> {
      List<ServiceInstance> windyClientInstances = discoverService.getWindyClientInstances();
      windyClientInstances.forEach(serviceInstance -> {
        String url = String.format(STOP_TASK_CLIENT_URL, serviceInstance.getHost());
        Request request = new Request.Builder().url(url)
            .put(RequestBody.create(mediaType, JSON.toJSONString(stopDispatch))).build();
        try {
          Response response = okHttpClient.newCall(request).execute();
          log.info("notify client stop result={}", response.body().string());
        } catch (IOException e) {
          log.error("notify client error");
        }
      });
    });
  }
}
