package com.zj.pipeline.git.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zj.pipeline.entity.enums.GitType;
import com.zj.pipeline.git.GitConstants;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.entity.vo.CreateBranchVo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Slf4j
@Service
public class GiteaRepositoryBranch implements IRepositoryBranch {

  private final GitRequestProxy gitRequestProxy;
  private Map<String, String> headers;

  public GiteaRepositoryBranch(GitRequestProxy gitRequestProxy) {
    this.gitRequestProxy = gitRequestProxy;
    String accessToken = gitRequestProxy.getGitAccess().getAccessToken();
    headers = new HashMap<>();
    headers.put("Authorization", accessToken);
  }

  @Override
  public String gitType() {
    return GitType.Gitea.name();
  }

  @Override
  public void createBranch(String serviceName, String branchName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format(GitConstants.CREATE_BRANCH, owner, serviceName);
    CreateBranchVo createBranchVO = new CreateBranchVo();
    createBranchVO.setBranchName(branchName);
    String result = gitRequestProxy.post(gitPath, JSON.toJSONString(createBranchVO), headers);
    log.info("gitea create branch result = {}", result);
  }

  @Override
  public void deleteBranch(String serviceName, String branchName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format(GitConstants.DELETE_BRANCH, owner, serviceName,
        branchName);
    String result = gitRequestProxy.delete(gitPath, headers);
    log.info("gitea delete branch result = {}", result);
  }

  @Override
  public List<String> listBranch(String serviceName) {
    String owner = gitRequestProxy.getGitAccess().getOwner();
    String gitPath = String.format(GitConstants.LIST_BRANCH, owner, serviceName);
    String result = gitRequestProxy.get(gitPath, headers);
    List<JSONObject> branches = JSON.parseArray(result, JSONObject.class);
    if (CollectionUtils.isEmpty(branches)) {
      return Collections.emptyList();
    }

    log.info("get list={}", result);
    return branches.stream().map(json -> json.getString("name"))
        .filter(branch -> !Objects.equals(branch, "master")).collect(Collectors.toList());
  }
}
