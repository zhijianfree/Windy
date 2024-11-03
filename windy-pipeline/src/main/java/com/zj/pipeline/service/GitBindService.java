package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.service.MicroserviceDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.pipeline.git.hook.IGitWebhook;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.git.RepositoryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService {

  private final IMicroServiceRepository microServiceRepository;
  private final PipelineService pipelineService;
  private final RepositoryFactory repositoryFactory;
  private final UniqueIdService uniqueIdService;
  private final IBindBranchRepository gitBindRepository;
  private final ISystemConfigRepository systemConfigRepository;
  private final Map<String, IGitWebhook> webhookMap;

  public GitBindService(IMicroServiceRepository microServiceRepository, PipelineService pipelineService,
                        RepositoryFactory repositoryFactory,
                        UniqueIdService uniqueIdService, IBindBranchRepository gitBindRepository, ISystemConfigRepository systemConfigRepository, List<IGitWebhook> webhooks) {
    this.microServiceRepository = microServiceRepository;
    this.pipelineService = pipelineService;
    this.repositoryFactory = repositoryFactory;
    this.uniqueIdService = uniqueIdService;
    this.gitBindRepository = gitBindRepository;
    this.systemConfigRepository = systemConfigRepository;
    webhookMap = webhooks.stream().collect(Collectors.toMap(IGitWebhook::platform, webhook -> webhook));
  }

  public String createGitBind(BindBranchBO bindBranchBO) {
    List<BindBranchBO> bindDtoList = listGitBinds(bindBranchBO.getPipelineId());
    Optional<BindBranchBO> optional = bindDtoList.stream()
        .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), bindBranchBO.getGitBranch()))
        .findAny();
    if (optional.isPresent()) {
      throw new ApiException(ErrorCode.BRANCH_ALREADY_BIND);
    }

    bindBranchBO.setBindId(uniqueIdService.getUniqueId());
    boolean result = gitBindRepository.saveGitBranch(bindBranchBO);
    return result ? bindBranchBO.getBindId() : "";
  }

  public List<BindBranchBO> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.getPipelineRelatedBranches(pipelineId);
  }

  @Transactional
  public Boolean updateGitBind(BindBranchBO bindBranchBO) {
    checkPipelineExist(bindBranchBO.getPipelineId());

    //解绑其他分支
    List<BindBranchBO> branches = gitBindRepository.getPipelineRelatedBranches(
        bindBranchBO.getPipelineId());
    List<String> unbindBranches = branches.stream().filter(
            branch -> branch.getIsChoose() && !Objects.equals(branch.getGitBranch(),
                bindBranchBO.getGitBranch())).map(BindBranchBO::getBindId)
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(unbindBranches)) {
      gitBindRepository.batchUnbindBranches(unbindBranches);
    }

    BindBranchBO gitBind = getGitBind(bindBranchBO.getBindId());
    if (Objects.isNull(gitBind)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
    }
    return gitBindRepository.updateGitBranch(bindBranchBO);
  }

  private BindBranchBO getGitBind(String bindId) {
    return gitBindRepository.getGitBranch(bindId);
  }

  public Boolean deleteGitBind(String pipelineId, String bindId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.deleteGitBranch(bindId);
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineBO pipelineBO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineBO)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }

  public void notifyHook(Object data, String platform) {
    IGitWebhook gitWebhook = webhookMap.get(platform);
    gitWebhook.webhook(data);
  }



  public List<String> getServiceBranch(String serviceId) {
    MicroserviceDto service = microServiceRepository.queryServiceDetail(serviceId);
    GitAccessInfo gitAccessInfo = Optional.ofNullable(service.getServiceConfig())
            .map(config -> JSON.parseObject(config, ServiceConfig.class))
            .map(ServiceConfig::getGitAccessInfo).filter(access -> StringUtils.isNotBlank(access.getAccessToken()))
            .orElseGet(systemConfigRepository::getGitAccess);
    IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
    gitAccessInfo.setGitUrl(service.getGitUrl());
    return repository.listBranch(gitAccessInfo);
  }
}
