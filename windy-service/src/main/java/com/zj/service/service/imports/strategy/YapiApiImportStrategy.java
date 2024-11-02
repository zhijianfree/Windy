package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.enums.ApiType;
import com.zj.domain.entity.bo.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.plugin.loader.ParamValueType;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.ApiResponse;
import com.zj.service.entity.YapiImportApi;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class YapiApiImportStrategy implements IApiImportStrategy {

    public static final String PROPERTIES_KEY = "properties";
    public static final String SPLIT_PREFIX = "_";
    public static final String HTTP_API_TYPE = "http";
    public static final String REQUIRED_KEY = "required";
    private final IServiceApiRepository serviceApiRepository;
    private final UniqueIdService uniqueIdService;
    private final Map<String, String> variableMap = new HashMap<>();

    public YapiApiImportStrategy(IServiceApiRepository serviceApiRepository, UniqueIdService uniqueIdService) {
        this.serviceApiRepository = serviceApiRepository;
        this.uniqueIdService = uniqueIdService;
        variableMap.put("string", "String");
        variableMap.put("integer", "Integer");
        variableMap.put("number", "Long");
        variableMap.put("boolean", "Boolean");
        variableMap.put("object", "Object");
        variableMap.put("array", "Array");
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
        String compareKey = getCompareKey(serviceApiDto.isApi(), serviceApiDto.getResource(),
                serviceApiDto.getMethod(), apiName);
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

            JSONObject jsonObject = JSON.parseObject(apiModel.getRequestBody());
            List<String> requirdList =
                    Optional.ofNullable(jsonObject).map(json -> JSON.parseArray(JSON.toJSONString(json.getJSONArray(
                            REQUIRED_KEY)), String.class)).orElseGet(Collections::emptyList);
            if (CollectionUtils.isNotEmpty(apiModel.getQueryParams())) {
                apiRequestVariables = apiModel.getQueryParams().stream().map(queryParam -> {
                    ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
                    apiRequestVariable.setParamKey(queryParam.getName());
                    apiRequestVariable.setDescription(queryParam.getDesc());
                    apiRequestVariable.setType("String");
                    apiRequestVariable.setRequired(requirdList.contains(queryParam.getName()));
                    apiRequestVariable.setPosition("Query");
                    return apiRequestVariable;
                }).collect(Collectors.toList());
            }

            log.info("api name={}", apiModel.getTitle());
            if (Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.getJSONObject(PROPERTIES_KEY))) {
                JSONObject properties = jsonObject.getJSONObject(PROPERTIES_KEY);
                List<ApiRequestVariable> bodyRequests = convertProperties(properties, requirdList);
                apiRequestVariables.addAll(bodyRequests);
            }
            serviceApi.setRequestParams(JSON.toJSONString(apiRequestVariables));

            JSONObject responseObject = JSON.parseObject(apiModel.getResBody());
            if (Objects.nonNull(responseObject) && Objects.nonNull(responseObject.getJSONObject(PROPERTIES_KEY))) {
                JSONObject resProperties = responseObject.getJSONObject(PROPERTIES_KEY);
                List<ApiResponse> apiResponses = resProperties.entrySet().stream().map(entry -> {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setParamKey(entry.getKey());
                    JSONObject typeJSON = JSON.parseObject(JSON.toJSONString(entry.getValue()), JSONObject.class);
                    apiResponse.setType(convertVariableType(typeJSON.getString("type")));
                    return apiResponse;
                }).collect(Collectors.toList());
                serviceApi.setResponseParams(JSON.toJSONString(apiResponses));
            }
            return saveOrUpdateApi(apiModel.getTitle(), serviceExistApi, serviceApi) ? serviceApi : null;
        } catch (Exception e) {
            log.info("import api error", e);
        }
        return null;
    }

    private List<ApiRequestVariable> convertProperties(JSONObject properties, List<String> requirdList) {
        return properties.entrySet().stream().map(entry -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(entry.getKey());
            JSONObject typeJSON = JSON.parseObject(JSON.toJSONString(entry.getValue()),
                    JSONObject.class);
            apiRequestVariable.setRequired(requirdList.contains(entry.getKey()));
            String type = convertVariableType(typeJSON.getString("type"));
            apiRequestVariable.setType(type);
            apiRequestVariable.setPosition("Body");

            if (Objects.equals(type, ParamValueType.Object.name())) {
                JSONObject subProperties = typeJSON.getJSONObject(PROPERTIES_KEY);
                List<String> subRequirdList =
                        Optional.of(typeJSON).map(json -> JSON.parseArray(JSON.toJSONString(json.getJSONArray(REQUIRED_KEY)), String.class)).orElseGet(Collections::emptyList);
                List<ApiRequestVariable> apiRequestVariables = convertProperties(subProperties, subRequirdList);
                apiRequestVariable.setChildren(apiRequestVariables);
            }

            if (Objects.equals(type, ParamValueType.Array.name())) {
                JSONObject item = typeJSON.getJSONObject("items");
                String itemType = convertVariableType(item.getString("type"));
                if (Objects.equals(itemType, ParamValueType.Object.name())) {
                    List<String> subRequirdList =
                            Optional.of(item).map(json -> JSON.parseArray(JSON.toJSONString(json.getJSONArray(
                                    REQUIRED_KEY)), String.class)).orElseGet(Collections::emptyList);
                    JSONObject subProperties = item.getJSONObject(PROPERTIES_KEY);
                    List<ApiRequestVariable> apiRequestVariables = convertProperties(subProperties, subRequirdList);
                    apiRequestVariable.setChildren(apiRequestVariables);
                }
            }
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private String convertVariableType(String type) {
        String stringType = variableMap.get(type);
        return StringUtils.isBlank(stringType) ? type : stringType;
    }
}
