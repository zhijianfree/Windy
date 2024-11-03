package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.MicroserviceBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/20
 */
public interface IMicroServiceRepository {

  String createService(String userId, MicroserviceBO microserviceBO);

  String updateService(MicroserviceBO microserviceBO);

  Boolean deleteService(String serviceId);

  MicroserviceBO queryServiceDetail(String serviceId);

  List<MicroserviceBO> getServices(String currentUserId);

  IPage<MicroserviceBO> getServices(Integer pageNo, Integer size, String name, List<String> serviceIds);

  MicroserviceBO queryServiceByName(String serviceName);

    List<MicroserviceBO> getAllServices();
}
