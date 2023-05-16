package com.zj.master.dispatch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.ResponseMeta;
import com.zj.common.utils.IpUtils;
import com.zj.master.entity.vo.BaseDispatch;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
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

  public void sendPipelineNodeTask(BaseDispatch baseDispatch) {
    String url = "http://WindyClient/v1/client/task";
    HttpEntity<BaseDispatch> http = new HttpEntity<>(baseDispatch);
    baseDispatch.setDispatchType("PIPELINE");
    baseDispatch.setMasterIp(IpUtils.getLocalIP());
    log.info("request body={}", JSON.toJSONString(baseDispatch));
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, http, String.class);
    log.info("get response status result ={}", responseEntity.getBody());
  }
}
