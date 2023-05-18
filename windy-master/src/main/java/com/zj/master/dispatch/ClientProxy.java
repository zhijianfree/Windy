package com.zj.master.dispatch;

import com.alibaba.fastjson.JSON;
import com.zj.common.utils.IpUtils;
import com.zj.master.entity.vo.BaseDispatch;
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

  public static final String DISPATCH_TASK_CLIENT = "http://WindyClient/v1/client/task";
  @Autowired
  private RestTemplate restTemplate;

  public void sendPipelineNodeTask(BaseDispatch baseDispatch) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<BaseDispatch> http = new HttpEntity<>(baseDispatch, headers);
    log.info("request body={}", JSON.toJSONString(baseDispatch));
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(DISPATCH_TASK_CLIENT, http, String.class);
    log.info("get response status result ={}", responseEntity.getBody());
  }
}
