package com.zj.feature.executor.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.feature.entity.dto.ParamDefine;
import com.zj.feature.entity.type.ParamTypeEnum;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.executor.vo.ExecutorUnit;
import com.zj.feature.utils.ExceptionUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class MethodInvoke implements IRemoteInvoker {

  public Object invoke(ExecutorUnit executorUnit) {
    try {
      Object[] objects = null;
      List<ParamDefine> paramDefines = executorUnit.getParams();
      if (!CollectionUtils.isEmpty(paramDefines)) {
        int i = 0;
        objects = new Object[paramDefines.size()];
        for (ParamDefine paramDefine : paramDefines) {
          objects[i] = convertDataToType(paramDefine);
          i++;
        }
      }

      Class<?> cls = Class.forName(executorUnit.getService());
      return MethodUtils.invokeMethod(ConstructorUtils.invokeConstructor(cls),
          executorUnit.getMethod(), objects);
    } catch (Exception e) {
      log.error("invoke method error", e);
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("invoke[ ")
          .append(executorUnit.getService())
          .append(" ] method [ ")
          .append(executorUnit.getMethod())
          .append(" ]\r\n").append("\tat").append(ExceptionUtils.getSimplifyError(e));
      ExecuteDetail executeDetail = new ExecuteDetail();
      executeDetail.setStatus(false);
      executeDetail.setErrorMessage(stringBuilder.toString());
      return executeDetail;
    }
  }

  public static Object convertDataToType(ParamDefine paramDefine) {
    if (Objects.equals(ParamTypeEnum.MAP.getType(), paramDefine.getType())) {
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
    MethodInvoke methodInvoke = new MethodInvoke();

    ExecutorUnit executorUnit = new ExecutorUnit();
    executorUnit.setMethod("startHttp");
    executorUnit.setService("com.zj.feature.ability.http.HttpFeature");
    executorUnit.setParams(JSON.parseArray(
        "[{\"paramKey\":\"url\",\"value\":\"http://10.58.239.162:8079/v5/iot/11111111111111111/devices/1234567890111w1qw\"},{\"paramKey\":\"method\",\"value\":\"delete\"},{\"paramKey\":\"headers\",\"type\":1,\"value\":{\"X_USER_INFO\":\"{\\\"domainId\\\": \\\"gyl\\\"}\",\"domainId\":\"huhuhu11111223\"}},{\"paramKey\":\"body\",\"value\":\"\"}]",
        ParamDefine.class));
    System.out.println(JSON.toJSONString(methodInvoke.invoke(executorUnit)));
    ;
  }
}
