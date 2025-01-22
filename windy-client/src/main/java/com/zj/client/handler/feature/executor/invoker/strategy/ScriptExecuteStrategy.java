package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.bo.ExecutePoint;
import com.zj.client.handler.feature.executor.vo.ScriptConfig;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.TemplateType;
import com.zj.common.exception.ExecuteException;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ScriptExecuteStrategy extends BaseExecuteStrategy {

    private final ScriptEngine engine;

    protected ScriptExecuteStrategy(InterceptorProxy interceptorProxy, List<IExecuteInvoker> executeInvokers,
                                    CompareHandler compareHandler) {
        super(interceptorProxy, executeInvokers, compareHandler);
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
    }

    @Override
    public List<TemplateType> getType() {
        return Collections.singletonList(TemplateType.SCRIPT);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
        log.info("start handle script context={}", JSON.toJSONString(featureExecuteContext.toMap()));
        FeatureResponse response;
        ExecutorUnit executorUnit = executePoint.getExecutorUnit();
        try {
            // 执行 JavaScript 代码
            String script = executorUnit.getService();
            Object result = executeJavaScript(featureExecuteContext, script);
            log.info("handle script result={}", JSON.toJSONString(result));

            //脚本执行结果添加到当前用例的上下文中
            Map<String, Object> resultMap = (Map<String, Object>) result;
            featureExecuteContext.bindMap(resultMap);
            ExecuteDetailVo executeDetail = new ExecuteDetailVo();
            executeDetail.setResBody(JSON.parseObject(JSON.toJSONString(result)));
            executeDetail.setStatus(true);
            response = FeatureResponse.builder().name(executorUnit.getName()).pointId(executePoint.getPointId())
                    .executeDetailVo(executeDetail).build();

            //如果配置了全局覆盖则将返回变量添加context中
            ScriptConfig scriptConfig = JSON.parseObject(executorUnit.getMethod(), ScriptConfig.class);
            log.info("log script config = {}", JSON.toJSONString(scriptConfig));
            if (Objects.nonNull(scriptConfig) && scriptConfig.getGlobal()) {
                log.info("script config cover global , then add result to response");
                response.setContext(resultMap);
            }
        } catch (Exception e) {
            log.info("execute javascript error", e);
            ExecuteDetailVo executeDetail = new ExecuteDetailVo();
            executeDetail.setErrorMessage(e.toString());
            executeDetail.setStatus(false);
            response = FeatureResponse.builder().name(executorUnit.getName()).pointId(executePoint.getPointId())
                    .executeDetailVo(executeDetail).build();
        }
        return Collections.singletonList(response);
    }

    private Object executeJavaScript(FeatureExecuteContext featureExecuteContext, String script) throws ScriptException {
        if (StringUtils.isBlank(script)) {
            log.info("execute script is empty, not execute");
            throw new ExecuteException("execute script is empty, not execute");
        }

        Bindings bindings = new SimpleBindings();
        Map<String, Object> context = featureExecuteContext.toMap();
        bindings.put("context", context);
        String execScript = "function execute(context) {" + script + "}execute(context);";
        Object result = engine.eval(execScript, bindings);
        if (!(result instanceof Map)) {
            log.info("after execute script , can not get map result");
            throw new ExecuteException("after execute script , can not get map result");
        }
        return result;
    }
}
