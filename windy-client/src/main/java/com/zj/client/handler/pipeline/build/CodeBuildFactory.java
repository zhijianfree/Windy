package com.zj.client.handler.pipeline.build;

import com.zj.common.exception.ExecuteException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CodeBuildFactory {

    private final Map<String, ICodeBuilder> codeBuilderMap;
    public CodeBuildFactory(List<ICodeBuilder> codeBuilders) {
        codeBuilderMap = codeBuilders.stream().collect(Collectors.toMap(ICodeBuilder::codeType, builder -> builder));
    }

    public ICodeBuilder getCodeBuilder(String codeType) {
        ICodeBuilder codeBuilder = codeBuilderMap.get(codeType);
        if (Objects.isNull(codeBuilder)) {
            throw new ExecuteException("can not find code builder code type=" + codeType);
        }
        return codeBuilder;
    }
}
