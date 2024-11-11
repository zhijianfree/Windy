package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import java.util.List;

/**
 * @author falcon
 * @since 2023/8/8
 */
public interface IServiceApiRepository {

  boolean saveApi(ServiceApiBO serviceApi);

  boolean saveBatch(List<ServiceApiBO> serviceApis);

  boolean updateApi(ServiceApiBO serviceApi);

  boolean deleteApi(String apiId);

  boolean batchDeleteApi(List<String> apiIds);

  ServiceApiBO getServiceApi(String apiId);

  List<ServiceApiBO> getApiByService(String serviceId);

  List<ServiceApiBO> getServiceApiList(List<String> apiIds);
}
