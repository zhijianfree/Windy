package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.enuns.ParamTypeEnum;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.client.handler.feature.executor.vo.ExecutorUnit;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.client.utils.ExceptionUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;

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
        instanceMap.put(featureDefine.getName(), featureDefine);
      });
    });
  }

  @Override
  public InvokerType type() {
    return InvokerType.METHOD;
  }

  public Object invoke(ExecutorUnit executorUnit) {
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

      return MethodUtils.invokeMethod(instance, executorUnit.getMethod(), objects);
    } catch (Exception e) {
      log.error("invoke method error", e);
      String stringBuilder = "invoke[ " + executorUnit.getService() + " ] method [ " + executorUnit.getMethod() + " ]\r\n" + "\tat" + ExceptionUtils.getSimplifyError(e);
      ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
      executeDetailVo.setStatus(false);
      executeDetailVo.setErrorMessage(stringBuilder);
      return executeDetailVo;
    }
  }

  public static Object convertDataToType(ParameterDefine paramDefine) {
    if (Objects.equals(ParamTypeEnum.MAP.getType(), paramDefine.getType())) {
      if (Objects.isNull(paramDefine.getValue())) {
        return new HashMap<>();
      }
      return (Map<String, Object>) JSONObject.parse(JSON.toJSONString(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.LIST.getType(), paramDefine.getType())) {
      return paramDefine.getValue();
    }

    if (Objects.equals(ParamTypeEnum.STRING.getType(), paramDefine.getType())) {
      return String.valueOf(paramDefine.getValue());
    }

    if (Objects.equals(ParamTypeEnum.INTEGER.getType(), paramDefine.getType())) {
      return Integer.parseInt(String.valueOf(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.FLOAT.getType(), paramDefine.getType())) {
      return Float.parseFloat(String.valueOf(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.DOUBLE.getType(), paramDefine.getType())) {
      return Double.parseDouble(String.valueOf(paramDefine.getValue()));
    }

    return paramDefine.getValue();

  }

  public static void main(String[] args) {
    MethodInvoke methodInvoke = new MethodInvoke(null);

    ExecutorUnit executorUnit = new ExecutorUnit();
    executorUnit.setMethod("startHttp");
    executorUnit.setService("com.zj.feature.ability.http.HttpFeature");
    executorUnit.setParams(JSON.parseArray(
        "[{\"paramKey\":\"url\",\"value\":\"http://10.58.239.162:8079/v5/iot/11111111111111111/devices/1234567890111w1qw\"},{\"paramKey\":\"method\",\"value\":\"delete\"},{\"paramKey\":\"headers\",\"type\":1,\"value\":{\"X_USER_INFO\":\"{\\\"domainId\\\": \\\"gyl\\\"}\",\"domainId\":\"huhuhu11111223\"}},{\"paramKey\":\"body\",\"value\":\"\"}]",
        ParameterDefine.class));
    System.out.println(JSON.toJSONString(methodInvoke.invoke(executorUnit)));
    ;
  }
}
