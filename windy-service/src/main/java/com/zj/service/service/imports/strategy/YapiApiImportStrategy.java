package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.service.ApiParamModel;
import com.zj.common.enums.ApiType;
import com.zj.common.enums.Position;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.ServiceApiBO;
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
        variableMap.put("string", ParamValueType.String.name());
        variableMap.put("integer", ParamValueType.Integer.name());
        variableMap.put("number", ParamValueType.Long.name());
        variableMap.put("boolean", ParamValueType.Boolean.name());
        variableMap.put("object", ParamValueType.Object.name());
        variableMap.put("array", ParamValueType.Array.name());
    }

    @Override
    public String importType() {
        return ImportType.Yapi.name();
    }

    @Override
    @Transactional
    public List<ServiceApiBO> importContent(String serviceId, String fileContent) {
        List<YapiImportApi> yapiImportApis = JSON.parseArray(fileContent, YapiImportApi.class);
        Map<String, ServiceApiBO> serviceExistApi = getServiceExistApi(serviceId);
        return yapiImportApis.stream().map(yapiImportApi -> {
            ServiceApiBO serviceApiBO = new ServiceApiBO();
            serviceApiBO.setApiId(uniqueIdService.getUniqueId());
            serviceApiBO.setApiName(yapiImportApi.getName());
            serviceApiBO.setApiType(ApiType.DIR.getType());
            serviceApiBO.setServiceId(serviceId);
            saveOrUpdateApi(yapiImportApi.getName(), serviceExistApi, serviceApiBO);


            List<YapiImportApi.YapiApiModel> list = yapiImportApi.getList();
            if (CollectionUtils.isEmpty(list)) {
                return Collections.singletonList(serviceApiBO);
            }
            return list.stream().map(apiModel -> convertApiAndSave(serviceId, apiModel, serviceApiBO, serviceExistApi))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }).filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private boolean saveOrUpdateApi(String apiName, Map<String, ServiceApiBO> serviceExistApi,
                                    ServiceApiBO serviceApiBO) {
        String compareKey = getCompareKey(serviceApiBO.isApi(), serviceApiBO.getResource(),
                serviceApiBO.getMethod(), apiName);
        ServiceApiBO existApi = serviceExistApi.get(compareKey);
        if (Objects.isNull(existApi)) {
            boolean result = serviceApiRepository.saveApi(serviceApiBO);
            log.info("create service api={} method={} result={}", serviceApiBO.getResource(),
                    serviceApiBO.getMethod(), result);
            return result;
        }

        serviceApiBO.setApiId(existApi.getApiId());
        boolean result = serviceApiRepository.updateApi(serviceApiBO);
        log.info("update service api={} method=" +
                "{} result={}", serviceApiBO.isApi() ? serviceApiBO.getResource() :
                serviceApiBO.getApiName(), serviceApiBO.getMethod(), result);
        return result;
    }

    private Map<String, ServiceApiBO> getServiceExistApi(String serviceId) {
        List<ServiceApiBO> apiList = serviceApiRepository.getApiByService(serviceId);
        return apiList.stream().collect(Collectors.toMap(api -> getCompareKey(api.isApi(), api.getResource(),
                api.getMethod(), api.getApiName()), api -> api));
    }

    public String getCompareKey(boolean isApi, String resource, String method, String apiName) {
        return isApi ? resource + SPLIT_PREFIX + method : apiName;
    }

    private ServiceApiBO convertApiAndSave(String serviceId, YapiImportApi.YapiApiModel apiModel,
                                           ServiceApiBO serviceApiBO, Map<String, ServiceApiBO> serviceExistApi) {
        try {
            ServiceApiBO serviceApi = new ServiceApiBO();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setApiType(ApiType.API.getType());
            serviceApi.setApiName(apiModel.getTitle());
            serviceApi.setMethod(apiModel.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(serviceApiBO.getApiId());
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
                    apiRequestVariable.setType(ParamValueType.String.name());
                    apiRequestVariable.setRequired(true);
                    apiRequestVariable.setPosition(Position.Path.name());
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
                    apiRequestVariable.setType(ParamValueType.String.name());
                    apiRequestVariable.setRequired(requirdList.contains(queryParam.getName()));
                    apiRequestVariable.setPosition(Position.Query.name());
                    return apiRequestVariable;
                }).collect(Collectors.toList());
            }

            log.info("api name={}", apiModel.getTitle());
            if (Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.getJSONObject(PROPERTIES_KEY))) {
                JSONObject properties = jsonObject.getJSONObject(PROPERTIES_KEY);
                List<ApiRequestVariable> bodyRequests = convertProperties(properties, requirdList);
                apiRequestVariables.addAll(bodyRequests);
            }
            serviceApi.setRequestParams(OrikaUtil.convertList(apiRequestVariables, ApiParamModel.class));

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

                serviceApi.setResponseParams(OrikaUtil.convertList(apiResponses, ApiParamModel.class));
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
            apiRequestVariable.setDescription(typeJSON.getString("description"));
            apiRequestVariable.setPosition(Position.Body.name());

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
                    ApiRequestVariable objectParam = new ApiRequestVariable();
                    objectParam.setParamKey("");
                    objectParam.setPosition(Position.Body.name());
                    objectParam.setType(ParamValueType.Object.name());
                    objectParam.setRequired(apiRequestVariable.isRequired());

                    List<String> subRequirdList =
                            Optional.of(item).map(json -> JSON.parseArray(JSON.toJSONString(json.getJSONArray(
                                    REQUIRED_KEY)), String.class)).orElseGet(Collections::emptyList);
                    JSONObject subProperties = item.getJSONObject(PROPERTIES_KEY);
                    List<ApiRequestVariable> apiRequestVariables = convertProperties(subProperties, subRequirdList);
                    objectParam.setChildren(apiRequestVariables);
                    apiRequestVariable.setChildren(Collections.singletonList(objectParam));
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
