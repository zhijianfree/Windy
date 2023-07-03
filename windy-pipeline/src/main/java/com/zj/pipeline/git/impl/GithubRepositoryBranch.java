package com.zj.pipeline.git.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.entity.enums.GitType;
import com.zj.pipeline.git.IRepositoryBranch;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubRepositoryBranch implements IRepositoryBranch {

  private final GitRequestProxy gitRequestProxy;

  public GithubRepositoryBranch(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
  }

  @Override
  public String gitType() {
    return GitType.Github.name();
  }

  @Override
  public boolean createBranch(String serviceName, String branchName) {
    return false;
  }

  @Override
  public boolean deleteBranch(String serviceName, String branchName) {
    return false;
  }

  @Override
  public List<String> listBranch(String serviceName) {
    String ownerName = gitRequestProxy.getGitAccess().getOwner();
    String path = String.format("/repos/%s/%s/branches", ownerName, serviceName);
    String result = gitRequestProxy.get(path);
    List<JSONObject> branches = JSON.parseArray(result, JSONObject.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    return branches.stream().map(json -> json.getString("name"))
        .filter(branch -> !Objects.equals(branch, "master")).collect(Collectors.toList());
  }

  public static void main(String[] args) {
    OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
        .connectTimeout(Duration.ofSeconds(30)).build();

    String url = "https://api.github.com/repos/zhijianfree/Windy/branches";
    Request request = new Request.Builder().url(url)
        .header("Authorization", "Bearer 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")
        .get().build();

    try {
      Response execute = okHttpClient.newCall(request).execute();
      if (!execute.isSuccessful()) {
        log.info("request result = {}", execute.body().string());
        throw new ApiException(ErrorCode.REQUEST_GIT_SERVER_FAILED);
      }
      System.out.println("=== " + execute.body().string());
    } catch (IOException e) {
      log.error("request git serve error", e);
    }
  }
}
