package com.zj.pipeline.git.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.entity.enums.GitType;
import com.zj.pipeline.git.IRepositoryBranch;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/7/3
 */
@Slf4j
@Component
public class GitlabRepositoryBranch implements IRepositoryBranch {

  private Map<String, String> headers;
  private GitRequestProxy gitRequestProxy;

  public GitlabRepositoryBranch(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
    this.headers = new HashMap<>();
    String accessToken = gitRequestProxy.getGitAccess().getAccessToken();
    headers.put("Private-Token", accessToken);
  }

  @Override
  public String gitType() {
    return GitType.Gitlab.name();
  }

  @Override
  public void createBranch(String serviceName, String branchName) {
    String path = String.format("/api/v4/projects/%s/repository/branches", serviceName);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("branch", branchName);
    jsonObject.put("ref", "master");
    String result = gitRequestProxy.post(branchName, JSON.toJSONString(jsonObject), headers);
    log.info("gitea create branch result = {}", result);
  }

  @Override
  public void deleteBranch(String serviceName, String branchName) {
    String path = String.format("/api/v4/projects/%s/repository/branches/%s", serviceName,
        branchName);
    String result = gitRequestProxy.delete(path, headers);
    log.info("gitea delete branch result = {}", result);
  }

  @Override
  public List<String> listBranch(String serviceName) {
    String path = String.format("/projects/%s/repository/branches", serviceName);
    String result = gitRequestProxy.get(path, headers);
    List<JSONObject> branches = JSON.parseArray(result, JSONObject.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    log.info("get list={}", result);
    return branches.stream().map(json -> json.getString("name"))
        .filter(branch -> !Objects.equals(branch, "master")).collect(Collectors.toList());
  }

  public static void main(String[] args) {
    OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
        .connectTimeout(Duration.ofSeconds(30)).build();

    String path = String.format("/projects/%s/repository/branches", "47345267");
    Request request = new Request.Builder().url("https://gitlab.com/api/v4" + path)
        .header("PRIVATE-TOKEN", "glpat-BJt61wWoBZWsyfspfsaw")
        .get().build();
    try {
      Response execute = okHttpClient.newCall(request).execute();
      String string = execute.body().string();
      System.out.println(string);
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
