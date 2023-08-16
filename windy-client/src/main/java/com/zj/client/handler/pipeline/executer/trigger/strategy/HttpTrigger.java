package com.zj.client.handler.pipeline.executer.trigger.strategy;


import com.alibaba.fastjson.JSON;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.*;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ExecuteException;
import com.zj.common.utils.OrikaUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Http请求处理
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpTrigger implements INodeTrigger {

  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
  public static final String RECORD_ID = "recordId";
  private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
      .readTimeout(10, TimeUnit.SECONDS)
      .connectTimeout(5, TimeUnit.SECONDS).build();

  @Override
  public ExecuteType type() {
    return ExecuteType.HTTP;
  }

  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws IOException {
    log.info("http executor is running");
    HttpRequestContext context = OrikaUtil.convert(triggerContext.getData(),
        HttpRequestContext.class);
    Map<String, Object> param = JSON.parseObject(context.getBody(), Map.class);
    param.put(RECORD_ID, taskNode.getRecordId());
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(param));
    Request request = new Request.Builder().url(context.getUrl()).post(requestBody).build();
    Response response = okHttpClient.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new ExecuteException(response.body().string());
    }
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    BaseExecuteParam baseExecuteParam = new BaseExecuteParam();
    baseExecuteParam.setRecordId(taskNode.getRecordId());
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(baseExecuteParam));
    Request request = new Request.Builder().url(refreshContext.getUrl()).post(requestBody)
        .headers(Headers.of(refreshContext.getHeaders())).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()) {
        return null;
      }

      return response.body().string();
    } catch (IOException e) {
      log.error("request http error", e);
    }
    return null;
  }
}
