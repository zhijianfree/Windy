package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.MicroserviceDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/20
 */
public interface IMicroServiceRepository {

  String createService(String userId, MicroserviceDto microserviceDto);

  String updateService(MicroserviceDto microserviceDto);

  Boolean deleteService(String serviceId);

  MicroserviceDto queryServiceDetail(String serviceId);

  List<MicroserviceDto> getServices(String currentUserId);

  IPage<MicroserviceDto> getServices(Integer pageNo, Integer size, String name, List<String> serviceIds);

  MicroserviceDto queryServiceByName(String serviceName);

    List<MicroserviceDto> getAllServices();
}
