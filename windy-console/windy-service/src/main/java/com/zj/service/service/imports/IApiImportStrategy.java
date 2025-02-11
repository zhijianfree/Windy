package com.zj.service.service.imports;

import com.zj.domain.entity.bo.service.ServiceApiBO;

import java.util.List;

public interface IApiImportStrategy {

    String importType();

    List<ServiceApiBO> importContent(String serviceId, String fileContent);
}
