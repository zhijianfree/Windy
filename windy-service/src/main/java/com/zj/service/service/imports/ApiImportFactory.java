package com.zj.service.service.imports;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApiImportFactory {
    private Map<String, IApiImportStrategy> strategyMap;

    public ApiImportFactory(List<IApiImportStrategy> strategies) {
        strategyMap = strategies.stream().collect(Collectors.toMap(IApiImportStrategy::importType,
                strategy -> strategy));
    }

    public IApiImportStrategy getImportStrategy(String importType) {
        return strategyMap.get(importType);
    }
}
