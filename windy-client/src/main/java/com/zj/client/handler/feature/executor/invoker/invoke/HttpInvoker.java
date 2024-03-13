package com.zj.client.handler.feature.executor.invoker.invoke;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.ability.http.HttpFeature;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.common.feature.ExecutorUnit;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.InvokerType;
import com.zj.plugin.loader.ExecuteDetailVo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/25
 */
@Slf4j
@Component
public class HttpInvoker implements IExecuteInvoker {

  private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

  @Override
  public InvokerType type() {
    return InvokerType.HTTP;
  }

  @Override
  public Object invoke(ExecutorUnit executorUnit) {
    log.info("get execute unit={}", JSON.toJSONString(executorUnit));
    Map<String, Object> bodyMap = getParamsMap(executorUnit);
    assemblyUrlParam(executorUnit, bodyMap);
    String url = executorUnit.getService();
    String method = executorUnit.getMethod();
    String body = JSON.toJSONString(bodyMap);
    Map<String, String> headers = Optional.ofNullable(executorUnit.getHeaders()).orElse(new HashMap<>());
    Request request = HttpFeature.requestFactory(url, method, headers, body);

    //执行并记录状态数据
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
    saveRequestInfo(executeDetailVo, url, method,body,headers);
    try (Response response = okHttpClient.newCall(request).execute();) {
      Optional.ofNullable(response.body()).ifPresent(responseBody -> {
        try {
          String string = responseBody.string();
          executeDetailVo.setResBody(JSON.parse(string));
        } catch (IOException e) {
          executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
        }
      });
      executeDetailVo.setStatus(response.code() == HttpStatus.OK.value());
    } catch (Exception e) {
      executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
    }
    return executeDetailVo;
  }

  private void saveRequestInfo(ExecuteDetailVo executeDetailVo, String url, String method, String body, Map<String, String> headers) {
    executeDetailVo.addRequestInfo("Url", url);
    executeDetailVo.addRequestInfo("Http Method", method);
    executeDetailVo.addRequestInfo("header", headers);
    executeDetailVo.addRequestInfo("body", body);
  }



  private void assemblyUrlParam(ExecutorUnit executorUnit, Map<String, Object> paramsMap) {
    StrSubstitutor strSubstitutor = new StrSubstitutor(paramsMap);
    String url = strSubstitutor.replace(executorUnit.getService());
    executorUnit.setService(url);
  }

  private static Map<String, Object> getParamsMap(ExecutorUnit executorUnit) {
    Map<String, Object> paramsMap = new HashMap<>();
    executorUnit.getParams().forEach(param -> paramsMap.put(param.getParamKey(), param.getValue()));
    return paramsMap;
  }
}
