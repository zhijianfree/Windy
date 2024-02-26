package com.zj.domain.repository.service;

import com.zj.domain.entity.dto.service.ServiceApiDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/8/8
 */
public interface IServiceApiRepository {

  boolean saveApi(ServiceApiDto serviceApi);

  boolean updateApi(ServiceApiDto serviceApi);

  boolean deleteApi(String apiId);

  boolean batchDeleteApi(List<String> apiIds);

  ServiceApiDto getServiceApi(String apiId);

  List<ServiceApiDto> getApiByService(String serviceId);

  List<ServiceApiDto> getServiceApiList(List<String> apiIds);
}
