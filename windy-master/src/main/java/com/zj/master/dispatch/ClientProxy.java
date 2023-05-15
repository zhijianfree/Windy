package com.zj.master.dispatch;

import com.alibaba.fastjson.JSON;
import com.zj.common.ResponseMeta;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Slf4j
@Component
public class ClientProxy {

  @Autowired
  private RestTemplate restTemplate;

  public void sendPipelineNodeTask(TaskNode taskNode) {
    String url = "http://windy-client/v1/client/task";
    ResponseEntity<ResponseMeta> responseEntity = restTemplate.postForEntity(url, taskNode,
        ResponseMeta.class);
    log.info("get response status result ={}", JSON.toJSONString(responseEntity.getBody()));
  }
}
