package com.zj.client.pipeline.executer.Invoker.strategy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.BaseExecuteParam;
import com.zj.client.pipeline.executer.vo.ExecuteType;
import com.zj.client.pipeline.executer.vo.HttpRequestContext;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpExecutor implements IRemoteInvoker {

  @Value("${server.port}")
  private Integer serverPort;
  private OgnlDataParser ognlDataParser = new OgnlDataParser();
  private Map<String, String> ipMap = new HashMap<>();
  public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
  private OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
      .connectTimeout(5, TimeUnit.SECONDS).build();

  public HttpExecutor() {
    ipMap.put("executeIp", IpUtils.getLocalIP() + serverPort);
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.HTTP;
  }

  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    log.info("http executor is running");
    HttpRequestContext context = OrikaUtil.convert(requestContext.getData(),
        HttpRequestContext.class);
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
    String url = exchangeExecuteIp(refreshContext.getUrl());
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE, JSON.toJSONString(baseExecuteParam));
    Request request = new Request.Builder().url(url).post(requestBody)
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

  /**
   * 为了实现请求地址变量转换
   */
  private String exchangeExecuteIp(String url) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    // http://
    String[] splits = url.split("//");
    if (splits.length < 2) {
      return url;
    }

    String domain = splits[1].split("/")[0];
    if (!domain.contains("$")) {
      return url;
    }

    return String.valueOf(ognlDataParser.parserExpression(ipMap, url.replace("$","#")));
  }
}
