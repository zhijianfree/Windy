package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.enums.InvokerType;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.IAsyncNotifyListener;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.ParameterDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class MethodInvoke implements IExecuteInvoker {

    private final Map<String, Object> instanceMap = new HashMap<>();

    private final PluginManager pluginManager;

    public MethodInvoke(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        new Thread(this::loadPluginFromDisk).start();
    }

    public void loadPluginFromDisk() {
        List<Feature> features = pluginManager.loadPlugins();
        features.forEach(feature -> {
            List<FeatureDefine> featureDefines = feature.scanFeatureDefines();
            if (CollectionUtils.isEmpty(featureDefines)) {
                return;
            }

            featureDefines.forEach(featureDefine -> {
                if (!instanceMap.containsKey(featureDefine.getSource())) {
                    log.info("get source ={} className={}", featureDefine.getSource(),
                            feature.getClass().getSimpleName());
                    instanceMap.put(featureDefine.getSource(), feature);
                }
            });
        });
    }

    @Override
    public InvokerType type() {
        return InvokerType.METHOD;
    }

    public Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext) {
        return invoke(executorUnit, featureExecuteContext, null);
    }

    @Override
    public Object invoke(ExecutorUnit executorUnit, FeatureExecuteContext featureExecuteContext,
                         IAsyncNotifyListener notifyListener) {
        try {
            Object[] objects = null;
            List<ParameterDefine> paramDefines = executorUnit.getParams();
            if (!CollectionUtils.isEmpty(paramDefines)) {
                int i = 0;
                objects = new Object[paramDefines.size()];
                for (ParameterDefine paramDefine : paramDefines) {
                    objects[i] = convertDataToType(paramDefine);
                    i++;
                }
            }

            Object instance = instanceMap.get(executorUnit.getService());
            if (Objects.isNull(instance)) {
                Class<?> cls = Class.forName(executorUnit.getService());
                instance = ConstructorUtils.invokeConstructor(cls);
                instanceMap.put(executorUnit.getName(), instance);
            }
            bindListenerIfNeed(notifyListener, instance);
            log.info("start invoke obj = {}", instance.getClass().getName());
            return MethodUtils.invokeMethod(instance, executorUnit.getMethod(), objects);
        } catch (Exception e) {
            log.error("invoke method error", e);
            String stringBuilder =
                    "invoke[ " + executorUnit.getService() + " ] method [ " + executorUnit.getMethod() + " ]\r\n" +
                            "\tat" + ExceptionUtils.getSimplifyError(e);
            ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
            executeDetailVo.setStatus(false);
            executeDetailVo.setErrorMessage(stringBuilder);
            return executeDetailVo;
        }
    }

    private void bindListenerIfNeed(IAsyncNotifyListener notifyListener, Object instance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (Objects.isNull(notifyListener)) {
            return;
        }
        String bindMethodName = "bindListener";
        Method method = MethodUtils.getMatchingMethod(instance.getClass(), bindMethodName, IAsyncNotifyListener.class);
        if (Objects.nonNull(method)) {
            MethodUtils.invokeMethod(instance, bindMethodName, notifyListener);
        }
    }

    public static void main(String[] args) {
        ParameterDefine parameterDefine = new ParameterDefine();
        parameterDefine.setType("Array");
        parameterDefine.setParamKey("appVersions");
        parameterDefine.setPosition("Body");
        parameterDefine.setDescription("dddd");
        parameterDefine.setValue(JSON.parseArray("[{\"imageId\":\"${imageId}\"," +
                "\"rangeList\":[{\"paramKey\":\"imageId\",\"initData\":{},\"position\":\"Body\",\"type\":\"String\",\"value\":\"${imageId}\"},{\"paramKey\":\"appName\",\"initData\":{},\"position\":\"Body\",\"type\":\"String\",\"value\":\"$RandomString(6)\"},{\"paramKey\":\"description\",\"initData\":{},\"position\":\"Body\",\"type\":\"String\",\"value\":\"111\"},{\"paramKey\":\"version\",\"initData\":{},\"position\":\"Body\",\"type\":\"String\",\"value\":\"1.0.0\"}],\"appName\":\"$RandomString(6)\",\"description\":\"111\",\"version\":\"1.0.0\"}]"));

        System.out.println(MethodInvoke.convertDataToType(parameterDefine));
    }
    public static Object convertDataToType(ParameterDefine paramDefine) {
        if (Objects.isNull(paramDefine.getValue())) {
            return null;
        }

        if (Objects.equals(ParamValueType.Map.name(), paramDefine.getType())) {
            if (Objects.isNull(paramDefine.getValue())) {
                return new HashMap<>();
            }
            return JSON.parse(JSON.toJSONString(paramDefine.getValue()));
        }

        if (Objects.equals(ParamValueType.Array.name(), paramDefine.getType())) {
            if (paramDefine.getValue() instanceof String) {
                return JSON.parseArray((String) paramDefine.getValue(), Object.class);
            }
            return paramDefine.getValue();
        }

        if (Objects.equals(ParamValueType.String.name(), paramDefine.getType())) {
            return String.valueOf(paramDefine.getValue());
        }

        if (Objects.equals(ParamValueType.Integer.name(), paramDefine.getType())) {
            return Integer.parseInt(String.valueOf(paramDefine.getValue()));
        }

        if (Objects.equals(ParamValueType.Long.name(), paramDefine.getType())) {
            return Long.parseLong(String.valueOf(paramDefine.getValue()));
        }

        if (Objects.equals(ParamValueType.Float.name(), paramDefine.getType())) {
            return Float.parseFloat(String.valueOf(paramDefine.getValue()));
        }

        if (Objects.equals(ParamValueType.Double.name(), paramDefine.getType())) {
            return Double.parseDouble(String.valueOf(paramDefine.getValue()));
        }

        return paramDefine.getValue();

    }
}
