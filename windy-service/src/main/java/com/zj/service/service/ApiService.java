package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.GenerateRecordDto;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.entity.dto.service.ServiceGenerateDto;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import com.zj.domain.repository.service.IGenerateRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ApiModel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
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
  private final IGenerateRepository generateRepository;
  private final IGenerateRecordRepository generateRecordRepository;

  public ApiService(UniqueIdService uniqueIdService, IServiceApiRepository apiRepository,
      RequestProxy requestProxy, ISystemConfigRepository systemConfigRepository,
      IGenerateRepository generateRepository, IGenerateRecordRepository generateRecordRepository) {
    this.uniqueIdService = uniqueIdService;
    this.apiRepository = apiRepository;
    this.requestProxy = requestProxy;
    this.systemConfigRepository = systemConfigRepository;
    this.generateRepository = generateRepository;
    this.generateRecordRepository = generateRecordRepository;
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

  public Boolean generateServiceApi(ServiceGenerateDto generate) {
    checkMavenConfig();
    saveOrUpdateParams(generate);
    DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
    dispatchTaskModel.setSourceId(generate.getServiceId());
    dispatchTaskModel.setType(LogType.GENERATE.getType());
    return requestProxy.runGenerate(dispatchTaskModel);
  }

  private void checkMavenConfig() {
    MavenConfigVo mavenConfig = systemConfigRepository.getMavenConfig();
    if (Objects.isNull(mavenConfig) || !mavenConfig.checkConfig()) {
      throw new ApiException(ErrorCode.MAVEN_NOT_CONFIG);
    }
  }

  private void saveOrUpdateParams(ServiceGenerateDto generate) {
    ServiceGenerateDto serviceGenerateDto = generateRepository.getByService(
        generate.getServiceId());
    if (Objects.isNull(serviceGenerateDto)) {
      generate.setGenerateId(uniqueIdService.getUniqueId());
      generateRepository.create(generate);
    }else{
      generateRepository.update(generate);
    }
  }

  public ServiceGenerateDto getGenerateParams(String serviceId) {
    return generateRepository.getByService(serviceId);
  }

  public List<GenerateRecordDto> getLatestGenerateLog(String serviceId) {
    List<GenerateRecordDto> serviceRecords = generateRecordRepository.getServiceRecords(serviceId);
    if (CollectionUtils.isEmpty(serviceRecords)) {
      return Collections.emptyList();
    }
    return serviceRecords.stream()
        .sorted(Comparator.comparing(GenerateRecordDto::getUpdateTime).reversed()).collect(
            Collectors.toList());
  }
}
