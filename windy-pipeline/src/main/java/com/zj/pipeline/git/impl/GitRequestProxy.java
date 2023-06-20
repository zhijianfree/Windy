package com.zj.pipeline.git.impl;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Slf4j
@Component
public class GitRequestProxy {

  public static final MediaType CONTENT_TYPE = MediaType.parse("application/json");
  public static final String AUTHORIZATION_KEY = "Authorization";
  public static final String TOKEN = "token e629641e97d843456303093c1fa5d92dcd3fd66e";
  private String remoteUrl = "http://localhost:3000";
  OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
      .connectTimeout(Duration.ofSeconds(30)).build();

  public String get(String path) {
    Request request = new Request.Builder().url(remoteUrl + path).header(AUTHORIZATION_KEY, TOKEN)
        .get().build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      if (!execute.isSuccessful()) {
        log.info("request result = {}", execute.body().string());
        throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
      }
      return execute.body().string();
    } catch (IOException e) {
      log.error("request git serve error", e);
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String post(String path, String body) {
    RequestBody requestBody = RequestBody.create(CONTENT_TYPE, body);

    Request request = new Request.Builder().url(remoteUrl + path).header(AUTHORIZATION_KEY, TOKEN)
        .post(requestBody).build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      if (!execute.isSuccessful()) {
        log.info("request result = {}", execute.body().string());
        throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
      }

      return execute.body().string();
    } catch (IOException e) {
      log.error("request git serve error", e);
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String put(String path, String body) {
    RequestBody requestBody = RequestBody.create(CONTENT_TYPE, body);

    Request request = new Request.Builder().url(remoteUrl + path).header(AUTHORIZATION_KEY, TOKEN)
        .put(requestBody).build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      if (!execute.isSuccessful()) {
        log.info("request result = {}", execute.body().string());
        throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
      }
      return execute.body().string();
    } catch (IOException e) {
      log.error("request git serve error", e);
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }

  public String delete(String path) {
    Request request = new Request.Builder().url(remoteUrl + path).header(AUTHORIZATION_KEY, TOKEN)
        .delete().build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      if (!execute.isSuccessful()) {
        log.info("request result = {}", execute.body().string());
        throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
      }
      return execute.body().string();
    } catch (IOException e) {
      log.error("request git serve error", e);
      throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
    }
  }
}
