package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.interceptor.VariableInterceptor;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.common.enums.InvokerType;
import com.zj.common.enums.Position;
import com.zj.common.feature.ExecutorUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class RelateTemplateInvoker implements IExecuteInvoker {
    private final MethodInvoke methodInvoke;
    private final VariableInterceptor variableInterceptor;

    public RelateTemplateInvoker(MethodInvoke methodInvoke, VariableInterceptor variableInterceptor) {
        this.methodInvoke = methodInvoke;
        this.variableInterceptor = variableInterceptor;
    }

    @Override
    public InvokerType type() {
        return InvokerType.RELATED_TEMPLATE;
    }

    @Override
    public Object invoke(ExecutorUnit executorUnit, ExecuteContext executeContext) {
        Map<String, Object> bodyMap = new HashMap<>();
        executorUnit.getParams().stream().filter(parameterDefine -> Objects.equals(parameterDefine.getPosition(),
                Position.Body.name())).forEach(param -> bodyMap.put(param.getParamKey(),
                MethodInvoke.convertDataToType(param)));
        String body = Optional.of(bodyMap).filter(map -> !map.isEmpty()).map(JSON::toJSONString).orElse("");

        ExecutorUnit relatedTemplate = executorUnit.getRelatedTemplate();
        relatedTemplate.getParams().forEach(param ->{
            if (Objects.equals(param.getParamKey(), "url")) {
                log.info("get request url = {}", executorUnit.getService());
                param.setValue(executorUnit.getService());
            }
            if (Objects.equals(param.getParamKey(), "body")) {
                param.setValue(body);
            }
            if (Objects.equals(param.getParamKey(), "header")) {
                param.setValue(executorUnit.getHeaders());
            }
            if (Objects.equals(param.getParamKey(), "method")) {
                param.setValue(executorUnit.getMethod());
            }
        });
        log.info("execute param={}", JSON.toJSONString(relatedTemplate));
        variableInterceptor.filterVariable(relatedTemplate, executeContext);
        log.info("filter execute param={}", JSON.toJSONString(relatedTemplate));
        return methodInvoke.invoke(relatedTemplate, executeContext);
    }
}
