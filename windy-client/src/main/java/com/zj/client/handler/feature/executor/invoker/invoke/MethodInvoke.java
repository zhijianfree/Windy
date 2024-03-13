package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.enuns.ParamTypeEnum;
import com.zj.common.enums.InvokerType;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.common.feature.ExecutorUnit;
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
        if (!instanceMap.containsKey(featureDefine.getSource())){
          instanceMap.put(featureDefine.getSource(), feature);
        }
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
      log.info("start invoke obj = {}", instance.getClass().getName());
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
    if (Objects.equals(ParamTypeEnum.Map.name(), paramDefine.getType())) {
      if (Objects.isNull(paramDefine.getValue())) {
        return new HashMap<>();
      }
      return JSONObject.parse(JSON.toJSONString(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.Array.name(), paramDefine.getType())) {
      return paramDefine.getValue();
    }

    if (Objects.equals(ParamTypeEnum.String.name(), paramDefine.getType())) {
      return String.valueOf(paramDefine.getValue());
    }

    if (Objects.equals(ParamTypeEnum.Integer.name(), paramDefine.getType())) {
      return Integer.parseInt(String.valueOf(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.Float.name(), paramDefine.getType())) {
      return Float.parseFloat(String.valueOf(paramDefine.getValue()));
    }

    if (Objects.equals(ParamTypeEnum.Double.name(), paramDefine.getType())) {
      return Double.parseDouble(String.valueOf(paramDefine.getValue()));
    }

    return paramDefine.getValue();

  }
}
