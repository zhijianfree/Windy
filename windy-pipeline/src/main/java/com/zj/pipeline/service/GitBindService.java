package com.zj.pipeline.service;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.common.git.IRepositoryBranch;
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
  private final Map<String, IGitWebhook> webhookMap;

  public GitBindService(IMicroServiceRepository microServiceRepository, PipelineService pipelineService,
      RepositoryFactory repositoryFactory,
      UniqueIdService uniqueIdService, IBindBranchRepository gitBindRepository, List<IGitWebhook> webhooks) {
    this.microServiceRepository = microServiceRepository;
    this.pipelineService = pipelineService;
    this.repositoryFactory = repositoryFactory;
    this.uniqueIdService = uniqueIdService;
    this.gitBindRepository = gitBindRepository;
    webhookMap = webhooks.stream().collect(Collectors.toMap(IGitWebhook::platform, webhook -> webhook));
  }

  public String createGitBind(BindBranchDto bindBranchDto) {
    List<BindBranchDto> bindDtoList = listGitBinds(bindBranchDto.getPipelineId());
    Optional<BindBranchDto> optional = bindDtoList.stream()
        .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), bindBranchDto.getGitBranch()))
        .findAny();
    if (optional.isPresent()) {
      throw new ApiException(ErrorCode.BRANCH_ALREADY_BIND);
    }

    bindBranchDto.setBindId(uniqueIdService.getUniqueId());
    boolean result = gitBindRepository.saveGitBranch(bindBranchDto);
    return result ? bindBranchDto.getBindId() : "";
  }

  public List<BindBranchDto> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.getPipelineRelatedBranches(pipelineId);
  }

  @Transactional
  public Boolean updateGitBind(BindBranchDto bindBranchDto) {
    checkPipelineExist(bindBranchDto.getPipelineId());

    //解绑其他分支
    List<BindBranchDto> branches = gitBindRepository.getPipelineRelatedBranches(
        bindBranchDto.getPipelineId());
    List<String> unbindBranches = branches.stream().filter(
            branch -> branch.getIsChoose() && !Objects.equals(branch.getGitBranch(),
                bindBranchDto.getGitBranch())).map(BindBranchDto::getBindId)
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(unbindBranches)) {
      gitBindRepository.batchUnbindBranches(unbindBranches);
    }

    BindBranchDto gitBind = getGitBind(bindBranchDto.getBindId());
    if (Objects.isNull(gitBind)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
    }
    return gitBindRepository.updateGitBranch(bindBranchDto);
  }

  private BindBranchDto getGitBind(String bindId) {
    return gitBindRepository.getGitBranch(bindId);
  }

  public Boolean deleteGitBind(String pipelineId, String bindId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.deleteGitBranch(bindId);
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineDto pipelineDTO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineDTO)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }

  public void notifyHook(Object data, String platform) {
    IGitWebhook gitWebhook = webhookMap.get(platform);
    gitWebhook.webhook(data);
  }



  public List<String> getServiceBranch(String serviceId) {
    MicroserviceDto serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
    IRepositoryBranch repository = repositoryFactory.getRepository();
    return repository.listBranch(serviceDetail.getServiceName());
  }
}
