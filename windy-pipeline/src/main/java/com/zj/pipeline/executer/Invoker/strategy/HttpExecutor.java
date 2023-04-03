package com.zj.pipeline.executer.Invoker.strategy;


import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.service.PipelineRecordService;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RequestContext;
import java.io.IOException;
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
 * @author falcon
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpExecutor implements IRemoteInvoker {

  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
  private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

  @Autowired
  private PipelineRecordService pipelineRecordService;

  public HttpExecutor(PipelineRecordService pipelineRecordService) {
    this.pipelineRecordService = pipelineRecordService;
  }

  @Override
  public String type() {
    return ExecuteType.HTTP.name();
  }

  public boolean triggerRun(RequestContext requestContext, String taskId) {
    log.info("http executor is running");
    HttpRequestContext context = (HttpRequestContext) requestContext.getContext();

    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, context.getBody());
    Request request = new Request.Builder().url(context.getUrl()).post(requestBody).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      return response.isSuccessful();
    } catch (IOException e) {
      log.error("request http error", e);
    }
    return false;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String taskId) {
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, "");
    Request request = new Request.Builder().url(refreshContext.getUrl()).post(requestBody).headers(
        Headers.of(refreshContext.getHeaders())).build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()){
        return null;
      }

      return response.toString();
    } catch (IOException e) {
      log.error("request http error", e);
    }
    return null;
  }
}
