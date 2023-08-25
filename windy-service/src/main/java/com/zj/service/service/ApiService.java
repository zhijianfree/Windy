package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ApiModel;
import java.util.List;
import java.util.Objects;
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
  private final RequestProxy requestProxy;
  private final ISystemConfigRepository systemConfigRepository;

  public ApiService(UniqueIdService uniqueIdService, IServiceApiRepository apiRepository,
      RequestProxy requestProxy, ISystemConfigRepository systemConfigRepository) {
    this.uniqueIdService = uniqueIdService;
    this.apiRepository = apiRepository;
    this.requestProxy = requestProxy;
    this.systemConfigRepository = systemConfigRepository;
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

  public Boolean generateServiceApi(String serviceId) {
    MavenConfigVo mavenConfig = systemConfigRepository.getMavenConfig();
    if (Objects.isNull(mavenConfig)) {
      throw new ApiException(ErrorCode.MERGE_CODE_ERROR);
    }
    DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
    dispatchTaskModel.setSourceId(serviceId);
    dispatchTaskModel.setType(LogType.GENERATE.getType());
    return requestProxy.runGenerate(dispatchTaskModel);
  }
}
