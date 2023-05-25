package com.zj.client.feature.executor.feature.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.entity.vo.RequestDetailVo;
import com.zj.client.feature.ability.http.HttpFeature;
import com.zj.client.feature.executor.feature.IExecuteInvoker;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.client.utils.ExceptionUtils;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/25
 */
@Component
public class HttpInvoker implements IExecuteInvoker {

  private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

  @Override
  public InvokerType type() {
    return InvokerType.HTTP;
  }

  @Override
  public Object invoke(ExecutorUnit executorUnit) {
    JSONObject jsonObject = new JSONObject();
    executorUnit.getParams().forEach(param -> {
      Object Value = MethodInvoke.convertDataToType(param);
      jsonObject.put(param.getParamKey(), Value);
    });

    String url = executorUnit.getService();
    String method = executorUnit.getMethod();
    String body = JSONObject.toJSONString(jsonObject);
    Map<String, String> headers = executorUnit.getHeaders();
    Request request = HttpFeature.requestFactory(url, method, headers, body);

    //执行并记录状态数据
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
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
    RequestDetailVo requestDetailVo = new RequestDetailVo();
    requestDetailVo.setRequestBody(body);
    executeDetailVo.setRequestDetailVo(requestDetailVo);

    return executeDetailVo;
  }
}
