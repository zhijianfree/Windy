package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.pipeline.entity.enums.PipelineExecuteType;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.git.hook.IGitWebhook;
import com.zj.service.service.MicroserviceService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService {

  private MicroserviceService microserviceService;
  private PipelineService pipelineService;

  private IRepositoryBranch repositoryBranch;
  private UniqueIdService uniqueIdService;
  private IBindBranchRepository gitBindRepository;

  private Map<String, IGitWebhook> webhookMap;

  public GitBindService(MicroserviceService microserviceService, PipelineService pipelineService,
      IRepositoryBranch repositoryBranch,
      UniqueIdService uniqueIdService, IBindBranchRepository gitBindRepository, List<IGitWebhook> webhooks) {
    this.microserviceService = microserviceService;
    this.pipelineService = pipelineService;
    this.repositoryBranch = repositoryBranch;
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
    MicroserviceDto serviceDetail = microserviceService.queryServiceDetail(serviceId);
    return repositoryBranch.listBranch(serviceDetail.getServiceName());
  }
}
