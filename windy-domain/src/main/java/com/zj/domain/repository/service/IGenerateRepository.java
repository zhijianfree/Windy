package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceGenerateBO;

public interface IGenerateRepository {

  /**
   * 获取服务生成信息
   * @param serviceId 服务ID
   * @return 生成信息
   */
  ServiceGenerateBO getByService(String serviceId);

  /**
   * 创建服务生成信息
   * @param serviceGenerateBO 生成信息
   * @return 是否成功
   */
  boolean create(ServiceGenerateBO serviceGenerateBO);

  /**
   * 更新服务生成信息
   * @param serviceGenerateBO 生成信息
   * @return 是否成功
   */
  boolean update(ServiceGenerateBO serviceGenerateBO);
}
