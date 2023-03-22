package com.zj.pipeline.executer.handler.strategy;


import com.zj.pipeline.service.NodeRecordService;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.handler.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RequestContext;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
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

  private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

  @Autowired
  private NodeRecordService nodeRecordService;

  public HttpExecutor(NodeRecordService nodeRecordService) {
    this.nodeRecordService = nodeRecordService;
  }

  @Override
  public String type() {
    return ExecuteType.HTTP.name();
  }

  public boolean execute(RequestContext requestContext, String taskId) {
    log.info("http executor is running");
    HttpRequestContext context = (HttpRequestContext) requestContext.getContext();

    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
        context.getBody());
    Request request = new Request.Builder().url(context.getUrl()).post(requestBody).build();
    ProcessStatus status = ProcessStatus.FAIL;
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (response.isSuccessful()) {
        status = ProcessStatus.SUCCESS;
      }
    } catch (IOException e) {
      log.error("request http error", e);
    }
    nodeRecordService.updateTaskStatus(taskId, status);
    return true;
  }
}
