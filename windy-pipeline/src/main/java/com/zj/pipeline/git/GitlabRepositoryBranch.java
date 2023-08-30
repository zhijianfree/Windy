package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.git.IRepositoryBranch;
import com.zj.pipeline.entity.vo.BranchInfo;
import com.zj.pipeline.entity.vo.GitlabRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/7/3
 */
@Slf4j
@Component
public class GitlabRepositoryBranch implements IRepositoryBranch {

  public static final String MASTER = "master";
  public static final String TEMP_PREFIX = "temp_";
  private final GitRequestProxy gitRequestProxy;

  private Map<String, Integer> serviceIdMap = new HashMap<>();

  public GitlabRepositoryBranch(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
    new Thread(this::loadGitRepositories).start();
  }

  private Map<String, String> getTokenHeader() {
    Map<String, String> header = new HashMap<>();
    String accessToken = gitRequestProxy.getGitAccess().getAccessToken();
    header.put("Private-Token", accessToken);
    return header;
  }

  private void loadGitRepositories() {
    try {
      List<GitlabRepository> gitlabRepositories = getGitlabRepositories();
      serviceIdMap = gitlabRepositories.stream()
          .collect(Collectors.toMap(repo -> repo.getName().toLowerCase(), GitlabRepository::getId));
    } catch (Exception e) {
      log.info("load gitlab repositories error ={}", e.getMessage());
    }
  }

  @Override
  public String gitType() {
    return GitType.Gitlab.name();
  }

  @Override
  public void createBranch(String serviceName, String branchName) {
    Integer projectId = transformProjectId(serviceName);
    String path = String.format("/api/v4/projects/%s/repository/branches?branch=%s&ref=master",
        projectId, branchName);
    String result = gitRequestProxy.post(path, "", getTokenHeader());
    log.info("gitea create branch result = {}", result);
    BranchInfo branchInfo = JSON.parseObject(result, BranchInfo.class);
    if (Objects.isNull(branchInfo) || !Objects.equals(branchInfo.getName(), branchName)) {
      throw new ApiException(ErrorCode.CREATE_BRANCH_ERROR);
    }

  }

  @Override
  public void deleteBranch(String serviceName, String branchName) {
    Integer projectId = transformProjectId(serviceName);
    String path = String.format("/api/v4/projects/%s/repository/branches/%s", projectId,
        branchName);
    String result = gitRequestProxy.delete(path, getTokenHeader());
    log.info("gitea delete branch result = {}", result);
  }

  @Override
  public List<String> listBranch(String serviceName) {
    Integer projectId = transformProjectId(serviceName);
    String path = String.format("/api/v4/projects/%s/repository/branches", projectId);
    String result = gitRequestProxy.get(path, getTokenHeader());
    List<BranchInfo> branches = JSON.parseArray(result, BranchInfo.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    log.info("get list={}", result);
    return branches.stream().map(BranchInfo::getName)
        //不显示master分支以及构建的临时分支
        .filter(branch -> !Objects.equals(branch, MASTER) && !branch.startsWith(TEMP_PREFIX))
        .collect(Collectors.toList());
  }

  private Integer transformProjectId(String serviceName) {
    Integer projectId = serviceIdMap.get(serviceName.toLowerCase());
    if (Objects.isNull(projectId)) {
      loadGitRepositories();
    }
    return serviceIdMap.get(serviceName.toLowerCase());
  }

  @Override
  public void checkRepository(String serviceName) {
    List<GitlabRepository> repositories = getGitlabRepositories();
    if (CollectionUtils.isEmpty(repositories)) {
      throw new ApiException(ErrorCode.REPO_NOT_EXIST);
    }

    Optional<GitlabRepository> optional = repositories.stream()
        .filter(repo -> Objects.equals(repo.getName().toLowerCase(), serviceName.toLowerCase()))
        .findAny();
    if (!optional.isPresent()) {
      throw new ApiException(ErrorCode.USER_NO_PERMISSION);
    }

    boolean permission = optional.get().getPermissions().checkPermission();
    if (!permission) {
      throw new ApiException(ErrorCode.GIT_NO_PERMISSION);
    }
  }

  private List<GitlabRepository> getGitlabRepositories() {
    String result = gitRequestProxy.get("/api/v4/projects", getTokenHeader());
    return JSON.parseArray(result, GitlabRepository.class);
  }
}
