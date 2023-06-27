package com.zj.common.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContextHolder;
import com.zj.common.model.ResultEvent;
import com.zj.common.model.StopDispatch;
import com.zj.common.monitor.discover.DiscoverService;
import com.zj.common.monitor.discover.ServiceInstance;
import com.zj.common.monitor.trace.TidInterceptor;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 请求代理类，后续可以在此类做扩展，对于master与client交互可以自定义策略 来执行调度哪个节点
 *
 * @author guyuelan
 * @since 2023/6/19
 */
@Slf4j
@Component
public class RequestProxy {

  private final RestTemplate restTemplate;
  private final DiscoverService discoverService;

  private static final String MASTER_DISPATCH_TASK = "http://WindyClient/v1/client/task";
  private static final String MASTER_NOTIFY_CLIENT_STOP = "http://%s/v1/client/task/stop";
  /**
   * client端执行流水线任务时使用
   */
  private static final String CLIENT_START_TASK = "http://WindyMaster/v1/devops/dispatch/task";
  private static final String CLIENT_NOTIFY_MASTER_URL = "http://WindyMaster/v1/devops/dispatch/notify";
  private static final String CLIENT_QUERY_APPROVAL_STATUS = "http://WindyMaster/v1/devops/master/record/";
  public static final String CONSOLE_RUN_TASK = "http://WindyMaster/v1/devops/dispatch/task";
  public static final String CONSOLE_STOP_TASK = "http://WindyMaster/v1/devops/dispatch/stop";
  private static final String WINDY_MASTER = "WindyMaster";
  private static final String WINDY_CLIENT = "WindyClient";
  private final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10,
      TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
  private final okhttp3.MediaType mediaType = okhttp3.MediaType.get(
      "application/json; charset=utf-8");
  private HttpHeaders headers;

  public RequestProxy(RestTemplate restTemplate, DiscoverService discoverService) {
    this.restTemplate = restTemplate;
    this.discoverService = discoverService;
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
  }


  /**
   * master向client分发子任务
   */
  public boolean sendDispatchTask(Object data, boolean isRequestSingle, String singleIp) {
    if (isRequestSingle) {
      ServiceInstance serviceInstance = discoverService.getServiceInstances(
              DiscoverService.WINDY_Client).stream()
          .filter(service -> Objects.equals(service.getIp(), singleIp)).findAny().orElse(null);
      return requestWithIp(data, serviceInstance);
    }

    wrapTraceHeader();
    HttpEntity<Object> http = new HttpEntity<>(data, headers);
    log.info("request body={}", JSON.toJSONString(data));
    try {
      ResponseEntity<String> response = restTemplate.postForEntity(MASTER_DISPATCH_TASK, http,
          String.class);
      log.info("get response status result ={}", response.getBody());
      return response.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.info("send dispatch task error", e);
    }
    return false;
  }

  private void wrapTraceHeader() {
    String traceId = MDC.get(TidInterceptor.MDC_TID_KEY);
    headers.add(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId);
  }

  private boolean requestWithIp(Object data, ServiceInstance serviceInstance) {
    if (Objects.isNull(serviceInstance)) {
      log.warn("can not find service instance");
      return false;
    }

    String traceId = MDC.get(TidInterceptor.MDC_TID_KEY);
    String url = MASTER_DISPATCH_TASK.replace(WINDY_CLIENT, serviceInstance.getHost());
    Request request = new Request.Builder().url(url)
        .header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId)
        .post(RequestBody.create(mediaType, JSON.toJSONString(data))).build();
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
   * 通知所有client节点停止节点
   */
  public void stopDispatchTask(StopDispatch stopDispatch) {
    CompletableFuture.runAsync(() -> {
      List<ServiceInstance> windyClientInstances = discoverService.getWindyClientInstances();
      windyClientInstances.forEach(serviceInstance -> {
        String url = String.format(MASTER_NOTIFY_CLIENT_STOP, serviceInstance.getHost());
        String traceId = MDC.get(TidInterceptor.MDC_TID_KEY);
        Request request = new Request.Builder().url(url)
            .header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId)
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

  /**
   * client触发用例任务执行
   */
  public ResponseEntity<JSONObject> startFeatureTask(Object data) {
    wrapTraceHeader();
    HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
    try {
      return restTemplate.postForEntity(CLIENT_START_TASK, httpEntity, JSONObject.class);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * client查询任务执行状态
   */
  public ResponseEntity<JSONObject> getFeatureTaskStatus(String url) {
    wrapTraceHeader();
    HttpEntity request = new HttpEntity(headers);
    return restTemplate.exchange(url, HttpMethod.GET, request, JSONObject.class);
  }

  /**
   * client子任务执行完成通知结果给master
   */
  public boolean clientNotifyEvent(ResultEvent resultEvent) {
    List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(WINDY_MASTER);
    Optional<ServiceInstance> optional = serviceInstances.stream().filter(
            serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
        .findFirst();
    if (optional.isPresent()) {
      // 如果触发任务执行的master节点存在那么优先访问触发任务的master节点
      return notifyWithMasterIP(resultEvent, optional.get());
    }

    //master节点不可达时，尝试使用其他的master节点
    wrapTraceHeader();
    HttpEntity<ResultEvent> httpEntity = new HttpEntity<>(resultEvent, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(CLIENT_NOTIFY_MASTER_URL,
        httpEntity, String.class);
    log.info("notify event code={} result={}", response.getStatusCode(), response.getBody());
    return response.getStatusCode().is2xxSuccessful();
  }

  private boolean notifyWithMasterIP(ResultEvent resultEvent, ServiceInstance serviceInstance) {
    String masterHost = serviceInstance.getHost();
    String url = CLIENT_NOTIFY_MASTER_URL.replace(WINDY_MASTER, masterHost);
    String traceId = MDC.get(TidInterceptor.MDC_TID_KEY);
    Request request = new Request.Builder().url(url)
        .header(TidInterceptor.HTTP_HEADER_TRACE_ID, traceId)
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
   * client审批节点查询审批的状态
   */
  public String getApprovalRecord(String recordId) {
    wrapTraceHeader();
    HttpEntity request = new HttpEntity(headers);
    String url = CLIENT_QUERY_APPROVAL_STATUS + recordId;
    ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request,
        String.class);
    return responseEntity.getBody();
  }

  /**
   * 控制台点击运行流水线
   */
  public String runPipeline(Object data) {
    wrapTraceHeader();
    HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
    try {
      ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(CONSOLE_RUN_TASK,
          httpEntity, JSONObject.class);
      JSONObject body = responseEntity.getBody();
      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          JSON.toJSONString(body));
      return body.getString("data");
    } catch (Exception e) {
      log.error("request dispatch pipeline task error", e);
    }
    return null;
  }

  /**
   * 控制台停止流水线
   */
  public boolean stopPipeline(Object data) {
    wrapTraceHeader();
    HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(CONSOLE_STOP_TASK,
          httpEntity, String.class);
      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          responseEntity.getBody());
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("request dispatch pipeline task error", e);
    }
    return false;
  }

  public boolean runTask(Object data) {
    wrapTraceHeader();
    HttpEntity<Object> httpEntity = new HttpEntity<>(data, headers);
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(CONSOLE_RUN_TASK,
          httpEntity, String.class);

      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          responseEntity.getBody());
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("request dispatch pipeline task error", e);
    }
    return false;
  }
}
