package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.common.enums.InvokerType;
import com.zj.common.feature.ExecutorUnit;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class RelateTemplateInvoker implements IExecuteInvoker {
    private final MethodInvoke methodInvoke;

    public RelateTemplateInvoker(MethodInvoke methodInvoke) {
        this.methodInvoke = methodInvoke;
    }

    @Override
    public InvokerType type() {
        return InvokerType.RELATED_TEMPLATE;
    }

    @Override
    public Object invoke(ExecutorUnit executorUnit) {
        Map<String, Object> paramMap = new HashMap<>();
        executorUnit.getParams().forEach(param -> paramMap.put(param.getParamKey(),
                MethodInvoke.convertDataToType(param)));
        String body = JSON.toJSONString(paramMap);

        ExecutorUnit relatedTemplate = executorUnit.getRelatedTemplate();
        relatedTemplate.getParams().forEach(param ->{
            if (Objects.equals(param.getParamKey(), "url")) {
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
        return methodInvoke.invoke(relatedTemplate);
    }
}
