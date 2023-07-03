package com.zj.pipeline.git.impl;

import com.alibaba.fastjson.JSON;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.pipeline.entity.vo.GitAccessVo;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
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
 * @since 2023/3/10
 */
@Slf4j
@Component
public class GitRequestProxy {

  public static final MediaType CONTENT_TYPE = MediaType.parse("application/json");
  public static final String AUTHORIZATION_KEY = "Authorization";
  public static final String GIT_CONFIG_ID = "2";
  OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
      .connectTimeout(Duration.ofSeconds(30)).build();
  private GitAccessVo gitAccess;

  public GitRequestProxy(ISystemConfigRepository systemRepository) {
    SystemConfigDto systemConfig = systemRepository.getSystemConfig(GIT_CONFIG_ID);
    gitAccess = JSON.parseObject(systemConfig.getConfigDetail(), GitAccessVo.class);
  }

  public String get(String path, Map<String, String> headers) {
    Request request = new Request.Builder().url(gitAccess.getGitDomain() + path)
        .headers(Headers.of(headers))
        .header(AUTHORIZATION_KEY, gitAccess.getAccessToken())
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

  public String post(String path, String body, Map<String, String> headers) {
    RequestBody requestBody = RequestBody.create(CONTENT_TYPE, body);

    Request request = new Request.Builder().url(gitAccess.getGitDomain() + path)
        .headers(Headers.of(headers))
        .header(AUTHORIZATION_KEY, gitAccess.getAccessToken())
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

    Request request = new Request.Builder().url(gitAccess.getGitDomain() + path)
        .header(AUTHORIZATION_KEY, gitAccess.getAccessToken())
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

  public String delete(String path, Map<String, String> headers) {
    Request request = new Request.Builder().url(gitAccess.getGitDomain() + path)
        .headers(Headers.of(headers))
        .header(AUTHORIZATION_KEY, gitAccess.getAccessToken())
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

  public GitAccessVo getGitAccess() {
    return gitAccess;
  }
}
