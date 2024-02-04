package com.zj.service.service.imports.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.PostmanImport;
import com.zj.service.service.imports.IApiImportStrategy;
import com.zj.service.service.imports.ImportType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PostmanImportStrategy implements IApiImportStrategy {

    private final UniqueIdService uniqueIdService;
    private final IServiceApiRepository serviceApiRepository;

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

        String name = postmanImport.getInfo().getName();
        ServiceApiDto serviceApiDto = new ServiceApiDto();
        serviceApiDto.setApiId(uniqueIdService.getUniqueId());
        serviceApiDto.setApiName(name);
        serviceApiDto.setIsApi(false);
        serviceApiDto.setServiceId(serviceId);
        serviceApiRepository.saveApi(serviceApiDto);

        postmanImport.getItem().forEach(postmanApi ->{
            PostmanImport.PostmanApiRequest request = postmanApi.getRequest();
            ServiceApiDto serviceApi = new ServiceApiDto();
            serviceApi.setApiId(uniqueIdService.getUniqueId());
            serviceApi.setIsApi(true);
            serviceApi.setApiName(postmanApi.getName());
            serviceApi.setMethod(request.getMethod());
            serviceApi.setServiceId(serviceId);
            serviceApi.setParentId(serviceApiDto.getApiId());
            serviceApi.setType("http");

//            String path = request.getUrl().getPath().stream().collect(Collectors.joining("/"));
//            request.getUrl().getQuery().stream().collect(Collectors.joining(""))
//            serviceApi.setResource(apiModel.getPath());
//            serviceApi.setHeader(JSON.toJSONString(apiModel.getHeaders()));
        });
        return null;
    }
}
