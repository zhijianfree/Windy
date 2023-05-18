package com.zj.master.dispatch;

import com.alibaba.fastjson.JSON;
import com.zj.master.entity.vo.BaseDispatch;
import com.zj.common.model.StopDispatch;
import lombok.extern.slf4j.Slf4j;
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

  public static final String DISPATCH_TASK_CLIENT_URL = "http://WindyClient/v1/client/task";
  public static final String STOP_TASK_CLIENT_URL = "http://WindyClient/v1/client/task/stop";
  @Autowired
  private RestTemplate restTemplate;

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

  public void stopDispatchTask(StopDispatch stopDispatch) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<StopDispatch> http = new HttpEntity<>(stopDispatch, headers);
    log.info("request body={}", JSON.toJSONString(stopDispatch));
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(STOP_TASK_CLIENT_URL, http,
          String.class);
      log.info("get response status result ={}", responseEntity.getBody());
    } catch (Exception e) {
      log.error("stop dispatch task error", e);
    }
  }
}
