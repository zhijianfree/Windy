package com.zj.client.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/16
 */
@Slf4j
@Component
public class ResultEventNotify implements IResultEventNotify {

  public static final String WINDY_MASTER = "WindyMaster";
  @Autowired
  private DiscoveryClient discoveryClient;

  @Override
  public void notifyEvent(ResultEvent resultEvent) {
    log.info("start notify result={} ", JSON.toJSONString(resultEvent));
    List<ServiceInstance> windyMaster = discoveryClient.getInstances(WINDY_MASTER);
    Optional<ServiceInstance> optional = windyMaster.stream().filter(
            serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
        .findFirst();
    if (!optional.isPresent()) {

    }

  }
}
