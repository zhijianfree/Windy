package com.zj.domain.repository.service;

import com.zj.domain.entity.dto.service.ServiceGenerateDto;

public interface IGenerateRepository {

  ServiceGenerateDto getByService(String serviceId);

  boolean create(ServiceGenerateDto serviceGenerateDto);

  boolean update(ServiceGenerateDto serviceGenerateDto);
}
