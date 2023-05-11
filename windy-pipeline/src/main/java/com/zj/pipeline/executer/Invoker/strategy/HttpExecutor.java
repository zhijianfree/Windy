package com.zj.pipeline.executer.Invoker.strategy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.executer.vo.BaseExecuteParam;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.service.NodeRecordService;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RequestContext;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpExecutor implements IRemoteInvoker {

  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
  private OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
      .connectTimeout(5, TimeUnit.SECONDS).build();

  @Autowired
  private NodeRecordService nodeRecordService;

  public HttpExecutor(NodeRecordService nodeRecordService) {
    this.nodeRecordService = nodeRecordService;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.HTTP;
  }

  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    log.info("http executor is running");
    HttpRequestContext context = (HttpRequestContext) requestContext.getContext();
    JSONObject jsonObject = JSON.parseObject(context.getBody());
    jsonObject.put("recordId", recordId);

    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(jsonObject));
    Request request = new Request.Builder().url(context.getUrl()).post(requestBody).build();
    Response response = okHttpClient.newCall(request).execute();
    return response.isSuccessful();
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    BaseExecuteParam baseExecuteParam = new BaseExecuteParam();
    baseExecuteParam.setRecordId(recordId);
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(baseExecuteParam));
    Request request = new Request.Builder().url(refreshContext.getUrl()).post(requestBody).headers(
        Headers.of(refreshContext.getHeaders())).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()){
        return null;
      }

      return response.body().string();
    } catch (IOException e) {
      log.error("request http error", e);
    }
    return null;
  }
}
