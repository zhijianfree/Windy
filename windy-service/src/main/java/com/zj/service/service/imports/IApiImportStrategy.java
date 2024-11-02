package com.zj.service.service.imports;

import com.zj.domain.entity.bo.service.ServiceApiDto;

import java.util.List;

public interface IApiImportStrategy {

    String importType();

    List<ServiceApiDto> importContent(String serviceId, String fileContent);
}
