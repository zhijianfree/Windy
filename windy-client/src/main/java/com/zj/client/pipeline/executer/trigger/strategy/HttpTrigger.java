package com.zj.client.pipeline.executer.trigger.strategy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.pipeline.executer.vo.BaseExecuteParam;
import com.zj.client.pipeline.executer.vo.HttpRequestContext;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.TriggerContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ExecuteException;
import com.zj.common.utils.OrikaUtil;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpTrigger implements INodeTrigger {

  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
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
    JSONObject jsonObject = JSON.parseObject(context.getBody());
    jsonObject.put("recordId", taskNode.getRecordId());

    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(jsonObject));
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