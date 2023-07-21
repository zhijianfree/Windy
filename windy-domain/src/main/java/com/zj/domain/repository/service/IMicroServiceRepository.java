package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/20
 */
public interface IMicroServiceRepository {

  String createService(MicroserviceDto microserviceDto);

  String updateService(MicroserviceDto microserviceDto);

  Boolean deleteService(String serviceId);

  MicroserviceDto queryServiceDetail(String serviceId);

  List<MicroserviceDto> getServices();

  IPage<MicroserviceDto> getServices(Integer pageNo, Integer size, String name);

  MicroserviceDto queryServiceByName(String serviceName);

  Integer countAll();
}
