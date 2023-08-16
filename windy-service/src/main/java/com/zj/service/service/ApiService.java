package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ApiModel;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Service
public class ApiService {

  private final UniqueIdService uniqueIdService;
  private final IServiceApiRepository apiRepository;

  public ApiService(UniqueIdService uniqueIdService, IServiceApiRepository apiRepository) {
    this.uniqueIdService = uniqueIdService;
    this.apiRepository = apiRepository;
  }

  public ServiceApiDto getServiceApi(String apiId) {
    return apiRepository.getServiceApi(apiId);
  }

  public List<ServiceApiDto> getServiceApis(String serviceId) {
    return apiRepository.getApiByService(serviceId);
  }

  public boolean createServiceApi(ApiModel apiModel) {
    ServiceApiDto serviceApi = OrikaUtil.convert(apiModel, ServiceApiDto.class);
    String requestParams = Optional.ofNullable(apiModel.getRequestParams())
        .map(JSON::toJSONString).orElse(null);
    serviceApi.setRequestParams(requestParams);

    String responseParams = Optional.ofNullable(apiModel.getResponseParams())
        .map(JSON::toJSONString).orElse(null);
    serviceApi.setResponseParams(responseParams);
    serviceApi.setApiId(uniqueIdService.getUniqueId());
    return apiRepository.saveApi(serviceApi);
  }

  public boolean updateServiceApi(ApiModel apiModel) {
    ServiceApiDto serviceApi = OrikaUtil.convert(apiModel, ServiceApiDto.class);
    serviceApi.setRequestParams(JSON.toJSONString(apiModel.getRequestParams()));
    serviceApi.setResponseParams(JSON.toJSONString(apiModel.getResponseParams()));
    return apiRepository.updateApi(serviceApi);
  }

  public boolean deleteServiceApi(String apiId) {
    return apiRepository.deleteApi(apiId);
  }

  public boolean batchDeleteApi(List<String> apiIds) {
    return apiRepository.batchDeleteApi(apiIds);
  }
}
