package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.MicroserviceBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/20
 */
public interface IMicroServiceRepository {

  /**
   * 创建服务
   * @param userId 用户ID
   * @param microserviceBO 服务信息
   * @return 服务ID
   */
  String createService(String userId, MicroserviceBO microserviceBO);

  /**
   * 更新服务
   * @param microserviceBO 服务信息
   * @return 是否成功
   */
  boolean updateService(MicroserviceBO microserviceBO);

  /**
   * 删除服务
   * @param serviceId 服务ID
   * @return 是否成功
   */
  Boolean deleteService(String serviceId);

  /**
   * 获取服务详情
   * @param serviceId 服务ID
   * @return 服务信息
   */
  MicroserviceBO queryServiceDetail(String serviceId);

  /**
   * 获取用户相关服务
   * @param currentUserId 用户ID
   * @return 服务列表
   */
  List<MicroserviceBO> getUserRelatedServices(String currentUserId);

  /**
   * 分页获取服务列表
   * @param pageNo 页码
   * @param size 每页数量
   * @param name 服务名称
   * @param serviceIds 服务ID列表
   * @return 服务列表
   */
  IPage<MicroserviceBO> getServices(Integer pageNo, Integer size, String name, List<String> serviceIds);

  /**
   * 根据服务名获取服务
   * @param serviceName 服务名
   * @return 服务信息
   */
  MicroserviceBO queryServiceByName(String serviceName);

  /**
   * 获取所有服务
   * @return 服务列表
   */
  List<MicroserviceBO> getAllServices();

  /**
   * 根据服务ID列表获取服务
   * @param serviceIdList 服务ID列表
   * @return 服务列表
   */
  List<MicroserviceBO> getServiceByIds(List<String> serviceIdList);
}
