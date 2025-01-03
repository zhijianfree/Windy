package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import java.util.List;

/**
 * @author falcon
 * @since 2023/8/8
 */
public interface IServiceApiRepository {

  /**
   * 保存API
   * @param serviceApi API信息
   * @return 是否成功
   */
  boolean saveApi(ServiceApiBO serviceApi);

  /**
   * 批量保存API
   * @param serviceApis API信息列表
   * @return 是否成功
   */
  boolean saveBatch(List<ServiceApiBO> serviceApis);

  /**
   * 更新API
   * @param serviceApi API信息
   * @return 是否成功
   */
  boolean updateApi(ServiceApiBO serviceApi);

  /**
   * 删除API
   * @param apiId API ID
   * @return 是否成功
   */
  boolean deleteApi(String apiId);

  /**
   * 批量删除API
   * @param apiIds API ID列表
   * @return 是否成功
   */
  boolean batchDeleteApi(List<String> apiIds);

  /**
   * 获取API详情
   * @param apiId API ID
   * @return API信息
   */
  ServiceApiBO getServiceApi(String apiId);

  /**
   * 获取服务下的API
   * @param serviceId 服务ID
   * @return API列表
   */
  List<ServiceApiBO> getApiByService(String serviceId);

  /**
   * 获取API列表
   * @param apiIds API ID列表
   * @return API列表
   */
  List<ServiceApiBO> getServiceApiList(List<String> apiIds);
}
