package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
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

  IPage<MicroserviceDto> getServices(Integer pageNo, Integer size, String name);

  MicroserviceDto queryServiceByName(String serviceName);

  boolean addServiceMember(String serviceId, String userId);

  List<UserDto> getServiceMembers(String serviceId);

  Boolean deleteServiceMember(String serviceId, String userId);
}
