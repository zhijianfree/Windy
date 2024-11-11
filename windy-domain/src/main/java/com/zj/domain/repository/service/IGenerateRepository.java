package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceGenerateBO;

public interface IGenerateRepository {

  ServiceGenerateBO getByService(String serviceId);

  boolean create(ServiceGenerateBO serviceGenerateBO);

  boolean update(ServiceGenerateBO serviceGenerateBO);
}
