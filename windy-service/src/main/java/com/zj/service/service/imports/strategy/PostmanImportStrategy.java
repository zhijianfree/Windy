package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.PostmanImport;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import com.zj.service.service.imports.Position;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return postmanImport.getItem().stream().map(postmanApi -> {
            PostmanImport.PostmanApiRequest request = postmanApi.getRequest();
            ServiceApiDto serviceApi = new ServiceApiDto();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setIsApi(true);
            serviceApi.setApiName(postmanApi.getName());
            serviceApi.setMethod(request.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(dirApi.getApiId());
            serviceApi.setType("http");

            String path = String.join("/", request.getUrl().getPath());
            serviceApi.setResource("/" + path);

            List<ApiRequestVariable> variableList = getRequestParams(request);
            serviceApi.setRequestParams(JSON.toJSONString(variableList));

            Map<String, String> headerMap =
                    postmanApi.getRequest().getHeader().stream().collect(Collectors.toMap(PostmanImport.PostmanApiHeader::getKey, PostmanImport.PostmanApiHeader::getValue));
            serviceApi.setHeader(JSON.toJSONString(headerMap));
            return serviceApiRepository.saveApi(serviceApi) ? serviceApi : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ServiceApiDto createApiDir(String serviceId, PostmanImport postmanImport) {
        String name = postmanImport.getInfo().getName();
        ServiceApiDto serviceApiDto = new ServiceApiDto();
        serviceApiDto.setApiId(uniqueIdService.getUniqueId());
        serviceApiDto.setApiName(name);
        serviceApiDto.setIsApi(false);
        serviceApiDto.setServiceId(serviceId);
        serviceApiRepository.saveApi(serviceApiDto);
        return serviceApiDto;
    }

    private List<ApiRequestVariable> getRequestParams(PostmanImport.PostmanApiRequest request) {
        List<ApiRequestVariable> pathVariableList = getPathVariableList(request.getUrl().getPath());
        List<ApiRequestVariable> variableList = new ArrayList<>(pathVariableList);

        List<ApiRequestVariable> bodyVariableList = getBodyVariableList(request);
        variableList.addAll(bodyVariableList);

        if (CollectionUtils.isNotEmpty(request.getUrl().getQuery())){
            List<ApiRequestVariable> queryVariableList = getQueryVariableList(request);
            variableList.addAll(queryVariableList);
        }
        return variableList;
    }

    private List<ApiRequestVariable> getQueryVariableList(PostmanImport.PostmanApiRequest request) {
        return request.getUrl().getQuery().stream().map(query -> {
            Pattern pattern = Pattern.compile(variableString);
            Matcher matcher = pattern.matcher(query.getValue());
            if (!matcher.find()) {
                return null;
            }
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(query.getKey());
            apiRequestVariable.setType("String");
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition(Position.Query.name());
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private List<ApiRequestVariable> getBodyVariableList(PostmanImport.PostmanApiRequest request) {
        if (Objects.isNull(request.getBody())) {
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSON.parseObject(request.getBody().getRaw(), JSONObject.class);
        return jsonObject.entrySet().stream().map(e -> {
            ApiRequestVariable apiRequestVariable = new ApiRequestVariable();
            apiRequestVariable.setParamKey(e.getKey());
            apiRequestVariable.setPosition(Position.Body.name());
            apiRequestVariable.setType(convertVariableType(e));
            return apiRequestVariable;
        }).collect(Collectors.toList());
    }

    private String convertVariableType(Map.Entry<String, Object> e) {
        if (e.getValue() instanceof String) {
            return "String";
        } else if (e.getValue() instanceof Integer) {
            return "Integer";
        } else if (e.getValue() instanceof Boolean) {
            return "Boolean";
        }else if (e.getValue() instanceof Long) {
            return "Long";
        } else if (e.getValue() instanceof Double || e.getValue() instanceof Float) {
            return "Float";
        } else if (e.getValue() instanceof JSONArray) {
            return "Array";
        } else if (e.getValue() instanceof JSONObject) {
            return "Object";
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
            apiRequestVariable.setType("String");
            apiRequestVariable.setRequired(true);
            apiRequestVariable.setPosition("Query");
            return apiRequestVariable;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
