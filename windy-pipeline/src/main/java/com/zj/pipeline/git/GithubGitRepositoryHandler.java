package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.git.GitAccessInfo;
import com.zj.common.git.IGitRepositoryHandler;
import com.zj.common.utils.GitUtils;
import com.zj.pipeline.entity.vo.GithubBranch;
import com.zj.pipeline.entity.vo.GithubRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GithubGitRepositoryHandler implements IGitRepositoryHandler {

    private final GitRequestProxy gitRequestProxy;
    private final OkHttpClient client = new OkHttpClient();

    public GithubGitRepositoryHandler(GitRequestProxy gitRequestProxy) {
        this.gitRequestProxy = gitRequestProxy;
    }

    @Override
    public String gitType() {
        return GitType.Github.name();
    }

    @Override
    public void createBranch(String branchName, GitAccessInfo accessInfo) {
        try {
            String baseBranchSHA = getBaseBranchSHA(accessInfo);
            boolean result = createBranch(baseBranchSHA, branchName, accessInfo);
            log.info("create branch = {} result={}", branchName, result);
        } catch (Exception e) {
            log.info("create github branch error", e);
            throw new ApiException(ErrorCode.CREATE_BRANCH_ERROR);
        }
    }

    @Override
    public void deleteBranch(String branchName, GitAccessInfo accessInfo) {
        String url = accessInfo.getGitDomain() + String.format("/repos/%s/%s/git/refs/heads/%s",
                accessInfo.getOwner(), accessInfo.getGitServiceName(), branchName);
        HashMap<String, String> headerMap = exchangeHeaders(accessInfo);
        String result = gitRequestProxy.delete(url, headerMap);
        log.info("delete github branch result={}", result);
    }

    @Override
    public List<String> listBranch(GitAccessInfo accessInfo) {
        String uri = String.format("/repos/%s/%s/branches", accessInfo.getOwner(), accessInfo.getGitServiceName());
        HashMap<String, String> hashMap = exchangeHeaders(accessInfo);
        String result = gitRequestProxy.get(accessInfo.getGitDomain() + uri, hashMap);
        List<GithubBranch> githubBranches = JSON.parseArray(result, GithubBranch.class);
        return Optional.ofNullable(githubBranches).map(branch -> branch.stream()
                        .map(GithubBranch::getName).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    private static HashMap<String, String> exchangeHeaders(GitAccessInfo accessInfo) {
        HashMap<String, String> hashMap = Maps.newHashMap();
        hashMap.put("Authorization", "Bearer " + accessInfo.getAccessToken());
        hashMap.put("X-GitHub-Api-Version", "2022-11-28");
        hashMap.put("Accept", "application/vnd.github+json");
        return hashMap;
    }

    @Override
    public void checkRepository(GitAccessInfo accessInfo) {
        String uri = String.format("/repos/%s/%s", accessInfo.getOwner(), accessInfo.getGitServiceName());
        HashMap<String, String> hashMap = exchangeHeaders(accessInfo);
        String result = gitRequestProxy.get(accessInfo.getGitDomain() + uri, hashMap);
        GithubRepository githubRepository = JSON.parseObject(result, GithubRepository.class);
        if (Objects.isNull(githubRepository)) {
            throw new ApiException(ErrorCode.REPO_NOT_EXIST);
        }
        log.info("get github repository result= {}", result);
    }

    // 获取指定分支的 SHA
    private String getBaseBranchSHA(GitAccessInfo accessInfo) throws IOException {
        String url = String.format("%s/repos/%s/%s/git/ref/heads/%s", accessInfo.getGitDomain(), accessInfo.getOwner(),
                accessInfo.getGitServiceName(), "master");
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(exchangeHeaders(accessInfo)))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get base branch SHA: " + response);
            }
            String responseBody = response.body().string();
            log.info("get github sha result = {}", responseBody);
            JSONObject json = JSON.parseObject(responseBody, JSONObject.class);
            JSONObject commit = json.getJSONObject("object");
            return commit.getString("sha");
        }catch (Exception e){
            log.info("get github sha error", e);
            throw e;
        }
    }

    /**
     * 创建新的分支。
     */
    public boolean createBranch(String sha, String branch, GitAccessInfo gitAccessInfo) throws IOException {
        String url = String.format("%s/repos/%s/%s/git/refs", gitAccessInfo.getGitDomain(), gitAccessInfo.getOwner(),
                gitAccessInfo.getGitServiceName());
        log.info("request create branch url = {}", url);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("ref", "refs/heads/" + branch);
        jsonBody.put("sha", sha);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(jsonBody));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .headers(Headers.of(exchangeHeaders(gitAccessInfo)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String errorBody = response.body() != null ? response.body().string() : "No response body";
            log.info("create github branch result = {}", errorBody);
            return response.isSuccessful();
        }catch (Exception e){
            log.info("create github branch error", e);
            throw e;
        }
    }
}
