package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.git.IRepositoryBranch;
import com.zj.pipeline.entity.vo.BranchInfo;
import com.zj.pipeline.entity.vo.CreateBranchVo;
import com.zj.pipeline.entity.vo.GiteaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Slf4j
@Service
public class GiteaRepositoryBranch implements IRepositoryBranch {

  private final GitRequestProxy gitRequestProxy;
  private final Map<String, String> headers;

  public GiteaRepositoryBranch(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
    String accessToken = gitRequestProxy.getGitAccess().getAccessToken();
    headers = new HashMap<>();
    headers.put("Authorization", "token " + accessToken);
  }

  @Override
  public String gitType() {
    return GitType.Gitea.name();
  }

  @Override
  public void createBranch(String serviceName, String branchName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches", owner, serviceName);
    CreateBranchVo createBranchVO = new CreateBranchVo();
    createBranchVO.setBranchName(branchName);
    String result = gitRequestProxy.post(gitPath, JSON.toJSONString(createBranchVO), headers);
    log.info("gitea create branch result = {}", result);
    BranchInfo branchInfo = JSON.parseObject(result, BranchInfo.class);
    if (Objects.isNull(branchInfo) || !Objects.equals(branchInfo.getName(), branchName)){
      throw new ApiException(ErrorCode.CREATE_BRANCH_ERROR);
    }

  }

  @Override
  public void deleteBranch(String serviceName, String branchName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches/%s", owner, serviceName,
        branchName);
    gitRequestProxy.delete(gitPath, headers);
  }

  @Override
  public void checkRepository(String serviceName) {
    String result = gitRequestProxy.get("/api/v1/user/repos", headers);
    log.info("query repository result ={}", result);
    List<GiteaRepository> repositories = JSON.parseArray(result, GiteaRepository.class);
    if (CollectionUtils.isEmpty(repositories)) {
      throw new ApiException(ErrorCode.REPO_NOT_EXIST);
    }
    Optional<GiteaRepository> optional = repositories.stream()
        .filter(repo -> Objects.equals(repo.getName(), serviceName)).findAny();
    if (!optional.isPresent()) {
      throw new ApiException(ErrorCode.USER_NO_PERMISSION);
    }

    boolean permission = optional.get().getPermissions().checkPermission();
    if (!permission) {
      throw new ApiException(ErrorCode.GIT_NO_PERMISSION);
    }
  }

  @Override
  public List<String> listBranch(String serviceName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format("/api/v1/repos/%s/%s/branches", owner, serviceName);
    String result = gitRequestProxy.get(gitPath, headers);
    List<BranchInfo> branches = JSON.parseArray(result, BranchInfo.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    log.info("get list={}", result);
    return branches.stream().map(BranchInfo::getName)
        .filter(branch -> !Objects.equals(branch, "master") && !branch.startsWith("temp_"))
        .collect(Collectors.toList());
  }
}
