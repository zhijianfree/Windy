package com.zj.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.QueryResponseModel;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.TestRequestContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class TestFeatureInvoker implements IRemoteInvoker {

  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
  public static final String START_TASK_URL = "http://localhost:9768/v1/devops/feature/task/%s";
  private OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
      .connectTimeout(5, TimeUnit.SECONDS).build();

  @Override
  public ExecuteType type() {
    return ExecuteType.TEST;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    TestRequestContext context = (TestRequestContext) requestContext;
    String taskId = context.getTaskId();
    String url = String.format(START_TASK_URL, taskId);
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, "");
    Request request = new Request.Builder().url(url).post(requestBody).build();
    Response response = okHttpClient.newCall(request).execute();
    return response.isSuccessful();
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    Request request = new Request.Builder().url(refreshContext.getUrl()).get().build();
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (!response.isSuccessful()){
        queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
        queryResponseModel.setMessage(Collections.singletonList("request http error"));
        return JSON.toJSONString(queryResponseModel);
      }

      return response.body().string();
    } catch (IOException e) {
      log.error("request http error", e);
      queryResponseModel.setMessage(getErrorMsg(e));
    }

    return JSON.toJSONString(queryResponseModel);
  }

  private List<String> getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return msg;
  }
}
