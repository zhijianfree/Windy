package com.zj.common.monitor.discover;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/23
 */
@Service
public class DiscoverService {

  public static final String WINDY_MASTER = "WindyMaster";
  public static final String WINDY_Client = "WindyClient";

  private DiscoveryClient discoveryClient;

  public DiscoverService(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  public ServiceInstance getWindyMasterByIp( String ip) {
    List<ServiceInstance> serviceInstances = getServiceInstances(WINDY_MASTER);
    return serviceInstances.stream().filter(instance -> Objects.equals(ip, instance.getIp()))
        .findFirst().orElse(null);
  }

  public ServiceInstance getWindyClientByIp( String ip) {
    List<ServiceInstance> serviceInstances = getServiceInstances(WINDY_Client);
    return serviceInstances.stream().filter(instance -> Objects.equals(ip, instance.getIp()))
        .findFirst().orElse(null);
  }

  public List<ServiceInstance> getWindyClientInstances(){
    return getServiceInstances(WINDY_Client);
  }

  public List<ServiceInstance> getServiceInstances(String serviceId) {
    return discoveryClient.getInstances(serviceId).stream().map(instance -> {
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setServiceId(instance.getServiceId());
      serviceInstance.setIp(instance.getHost());
      serviceInstance.setPort(instance.getPort());
      serviceInstance.setHost(instance.getHost() + ":" + instance.getPort());
      return serviceInstance;
    }).collect(Collectors.toList());
  }
}
