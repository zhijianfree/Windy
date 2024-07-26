package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ApiType;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.ApiResponse;
import com.zj.service.entity.YapiImportApi;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class YapiApiImportStrategy implements IApiImportStrategy {

    public static final String PROPERTIES_KEY = "properties";
    public static final String SPLIT_PREFIX = "_";
    public static final String HTTP_API_TYPE = "http";
    private final IServiceApiRepository serviceApiRepository;
    private final UniqueIdService uniqueIdService;

    public YapiApiImportStrategy(IServiceApiRepository serviceApiRepository, UniqueIdService uniqueIdService) {
        this.serviceApiRepository = serviceApiRepository;
        this.uniqueIdService = uniqueIdService;
    }

    @Override
    public String importType() {
        return ImportType.Yapi.name();
    }

    @Transactional
    @Override
    public List<ServiceApiDto> importContent(String serviceId, String fileContent) {
        List<YapiImportApi> yapiImportApis = JSON.parseArray(fileContent, YapiImportApi.class);
        Map<String, ServiceApiDto> serviceExistApi = getServiceExistApi(serviceId);
        return yapiImportApis.stream().map(yapiImportApi -> {
            ServiceApiDto serviceApiDto = new ServiceApiDto();
            serviceApiDto.setApiId(uniqueIdService.getUniqueId());
            serviceApiDto.setApiName(yapiImportApi.getName());
            serviceApiDto.setApiType(ApiType.DIR.getType());
            serviceApiDto.setServiceId(serviceId);
            saveOrUpdateApi(yapiImportApi.getName(), serviceExistApi, serviceApiDto);


            List<YapiImportApi.YapiApiModel> list = yapiImportApi.getList();
            if (CollectionUtils.isEmpty(list)) {
                return Collections.singletonList(serviceApiDto);
            }
            return list.stream().map(apiModel -> convertApiAndSave(serviceId, apiModel, serviceApiDto, serviceExistApi))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }).filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private boolean saveOrUpdateApi(String apiName, Map<String, ServiceApiDto> serviceExistApi,
                                 ServiceApiDto serviceApiDto) {
        String compareKey = getCompareKey(serviceApiDto.isApi(), serviceApiDto.getResource(), serviceApiDto.getMethod(), apiName);
        ServiceApiDto existApi = serviceExistApi.get(compareKey);
        if (Objects.isNull(existApi)) {
            boolean result = serviceApiRepository.saveApi(serviceApiDto);
            log.info("create service api={} method={} result={}", serviceApiDto.getResource(),
                    serviceApiDto.getMethod(), result);
            return result;
        }

        serviceApiDto.setApiId(existApi.getApiId());
        boolean result = serviceApiRepository.updateApi(serviceApiDto);
        log.info("update service api={} method=" +
                "{} result={}", serviceApiDto.isApi() ? serviceApiDto.getResource() :
                        serviceApiDto.getApiName(), serviceApiDto.getMethod(), result);
        return result;
    }

    private Map<String, ServiceApiDto> getServiceExistApi(String serviceId) {
        List<ServiceApiDto> apiList = serviceApiRepository.getApiByService(serviceId);
        return apiList.stream().collect(Collectors.toMap(api -> getCompareKey(api.isApi(), api.getResource(),
                        api.getMethod(), api.getApiName()), api -> api));
    }

    public String getCompareKey(boolean isApi, String resource, String method, String apiName) {
        return isApi ? resource + SPLIT_PREFIX + method : apiName;
    }

    private ServiceApiDto convertApiAndSave(String serviceId, YapiImportApi.YapiApiModel apiModel,
                                            ServiceApiDto serviceApiDto, Map<String, ServiceApiDto> serviceExistApi) {
        try {
            ServiceApiDto serviceApi = new ServiceApiDto();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setApiType(ApiType.API.getType());
            serviceApi.setApiName(apiModel.getTitle());
            serviceApi.setMethod(apiModel.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(serviceApiDto.getApiId());
            serviceApi.setDescription(apiModel.getTitle());
            serviceApi.setType(HTTP_API_TYPE);
            serviceApi.setResource(apiModel.getPath());
            serviceApi.setHeader(JSON.toJSONString(apiModel.getHeaders()));

            List<ApiRequestVariable> apiRequestVariables = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(apiModel.getPathParams())) {
                apiRequestVariables = apiModel.getPathParams().stream().map(pathParam -> {
                    ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
                    apiRequestVariable.setParamKey(pathParam.getName());
                    apiRequestVariable.setDescription(pathParam.getDesc());
                    apiRequestVariable.setType("String");
                    apiRequestVariable.setRequired(true);
                    apiRequestVariable.setPosition("Path");
                    return apiRequestVariable;
                }).collect(Collectors.toList());
            }

            log.info("api name={}", apiModel.getTitle());
            JSONObject jsonObject = JSON.parseObject(apiModel.getRequestBody());
            if (Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.getJSONObject(PROPERTIES_KEY))) {
                JSONObject properties = jsonObject.getJSONObject(PROPERTIES_KEY);
                List<String> requirdList = JSON.parseArray(JSON.toJSONString(jsonObject.getJSONArray("required")), String.class);
                List<ApiRequestVariable> bodyRequests = properties.entrySet().stream().map(entry -> {
                    ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
                    apiRequestVariable.setParamKey(entry.getKey());

                    JSONObject typeJSON = JSON.parseObject(JSON.toJSONString(entry.getValue()),
                            JSONObject.class);
                    apiRequestVariable.setRequired(requirdList.contains(entry.getKey()));
                    apiRequestVariable.setType(typeJSON.getString("type"));
                    apiRequestVariable.setPosition("Body");
                    return apiRequestVariable;
                }).collect(Collectors.toList());
                apiRequestVariables.addAll(bodyRequests);
            }
            serviceApi.setRequestParams(JSON.toJSONString(apiRequestVariables));

            JSONObject responseObject = JSON.parseObject(apiModel.getResBody());
            if (Objects.nonNull(responseObject)) {
                JSONObject resProperties = responseObject.getJSONObject(PROPERTIES_KEY);
                List<ApiResponse> apiResponses = resProperties.entrySet().stream().map(entry -> {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setParamKey(entry.getKey());

                    JSONObject typeJSON = JSON.parseObject(JSON.toJSONString(entry.getValue()),
                            JSONObject.class);
                    apiResponse.setType(typeJSON.getString("type"));
                    return apiResponse;
                }).collect(Collectors.toList());
                serviceApi.setResponseParams(JSON.toJSONString(apiResponses));
            }
            return saveOrUpdateApi(apiModel.getTitle(), serviceExistApi, serviceApi) ? serviceApi : null;
        } catch (Exception e) {
            log.info("import api error");
        }
        return null;
    }
}
