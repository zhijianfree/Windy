package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ApiType;
import com.zj.common.enums.Position;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.plugin.loader.ParamValueType;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.PostmanImport;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostmanImportStrategy implements IApiImportStrategy {
    private final UniqueIdService uniqueIdService;
    private final IServiceApiRepository serviceApiRepository;
    private final String variableString = "\\{\\{([^\\}]+)\\}\\}";

    public PostmanImportStrategy(UniqueIdService uniqueIdService, IServiceApiRepository serviceApiRepository) {
        this.uniqueIdService = uniqueIdService;
        this.serviceApiRepository = serviceApiRepository;
    }

    @Override
    public String importType() {
        return ImportType.Postman.name();
    }

    @Override
    public List<ServiceApiDto> importContent(String serviceId, String fileContent) {
        PostmanImport postmanImport = JSON.parseObject(fileContent, PostmanImport.class);
        if (Objects.isNull(postmanImport)) {
            return Collections.emptyList();
        }

        ServiceApiDto dirApi = createApiDir(serviceId, postmanImport);
        List<ServiceApiDto> serviceApiList = postmanImport.getItem().stream().map(postmanApi -> {
            PostmanImport.PostmanApiRequest request = postmanApi.getRequest();
            ServiceApiDto serviceApi = new ServiceApiDto();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setApiType(ApiType.API.getType());
            serviceApi.setApiName(postmanApi.getName());
            serviceApi.setDescription(postmanApi.getName());
            serviceApi.setMethod(request.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(dirApi.getApiId());
            serviceApi.setType("http");

            Optional.ofNullable(request.getUrl()).ifPresent(url -> {
                String path = String.join("/", request.getUrl().getPath());
                serviceApi.setResource("/" + path);
            });


            List<ApiRequestVariable> variableList = getRequestParams(request);
            serviceApi.setRequestParams(JSON.toJSONString(variableList));

            Optional.ofNullable(request.getHeader()).ifPresent(headers -> {
                Map<String, String> headerMap =
                        headers.stream().collect(Collectors.toMap(PostmanImport.PostmanApiHeader::getKey,
                                PostmanImport.PostmanApiHeader::getValue));
                serviceApi.setHeader(JSON.toJSONString(headerMap));
            });
            return serviceApi;
        }).collect(Collectors.toList());
        boolean saveBatch = serviceApiRepository.saveBatch(serviceApiList);
        log.info("batch save postman api result = {}", saveBatch);
        return serviceApiList;
    }

    private ServiceApiDto createApiDir(String serviceId, PostmanImport postmanImport) {
        String name = postmanImport.getInfo().getName();
        ServiceApiDto serviceApiDto = new ServiceApiDto();
        serviceApiDto.setApiId(uniqueIdService.getUniqueId());
        serviceApiDto.setApiName(name);
        serviceApiDto.setApiType(ApiType.DIR.getType());
        serviceApiDto.setServiceId(serviceId);
        serviceApiRepository.saveApi(serviceApiDto);
        return serviceApiDto;
    }

    private List<ApiRequestVariable> getRequestParams(PostmanImport.PostmanApiRequest request) {
        PostmanImport.PostmanApiUrl postmanApiUrl = request.getUrl();
        List<ApiRequestVariable> variableList = new ArrayList<>();
        if (Objects.nonNull(postmanApiUrl)){
            List<ApiRequestVariable> pathVariableList = getPathVariableList(postmanApiUrl.getPath());
            variableList.addAll(pathVariableList);

            if (CollectionUtils.isNotEmpty(postmanApiUrl.getQuery())) {
                List<ApiRequestVariable> queryVariableList = getQueryVariableList(request);
                variableList.addAll(queryVariableList);
            }
        }

        List<ApiRequestVariable> bodyVariableList = getBodyVariableList(request);
        variableList.addAll(bodyVariableList);
        return variableList;
    }

    private List<ApiRequestVariable> getQueryVariableList(PostmanImport.PostmanApiRequest request) {
        return request.getUrl().getQuery().stream().map(query -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(query.getKey());
            apiRequestVariable.setType(ParamValueType.String.name());
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition(Position.Query.name());


            Pattern pattern = Pattern.compile(variableString);
            Matcher matcher = pattern.matcher(query.getValue());
            if (!matcher.find()) {
                return apiRequestVariable;
            }
            apiRequestVariable.setDefaultValue(query.getValue());
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private List<ApiRequestVariable> getBodyVariableList(PostmanImport.PostmanApiRequest request) {
        if (Objects.isNull(request.getBody())) {
            return Collections.emptyList();
        }

        JSONObject jsonObject = parseJsonString(request);
        if (Objects.isNull(jsonObject)) {
            return Collections.emptyList();
        }

        return jsonObject.entrySet().stream().map(e -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(e.getKey());
            apiRequestVariable.setPosition(Position.Body.name());
            apiRequestVariable.setType(convertVariableType(e));
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private JSONObject parseJsonString(PostmanImport.PostmanApiRequest request) {
        try {
            return JSON.parseObject(request.getBody().getRaw(), JSONObject.class);
        }catch (Exception e){
            log.info("convert json error", e);
        }
        return null;
    }

    private String convertVariableType(Map.Entry<String, Object> e) {
        if (e.getValue() instanceof String) {
            return ParamValueType.String.name();
        } else if (e.getValue() instanceof Integer) {
            return ParamValueType.Integer.name();
        } else if (e.getValue() instanceof Boolean) {
            return ParamValueType.Boolean.name();
        } else if (e.getValue() instanceof Long) {
            return ParamValueType.Long.name();
        } else if (e.getValue() instanceof Double || e.getValue() instanceof Float) {
            return ParamValueType.Float.name();
        } else if (e.getValue() instanceof JSONArray) {
            return ParamValueType.Array.name();
        } else if (e.getValue() instanceof JSONObject) {
            return ParamValueType.Object.name();
        } else {
            return null;
        }
    }

    private List<ApiRequestVariable> getPathVariableList(List<String> variableList) {
        return variableList.stream().map(string -> {
            Pattern pattern = Pattern.compile(variableString);
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find()) {
                return null;
            }
            String paramName = matcher.group(1);
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(paramName);
            apiRequestVariable.setType(ParamValueType.String.name());
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition(Position.Path.name());
            return apiRequestVariable;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
