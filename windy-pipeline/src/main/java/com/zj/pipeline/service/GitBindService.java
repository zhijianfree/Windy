package com.zj.pipeline.service;

import com.zj.common.adapter.git.CommitMessage;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import com.zj.pipeline.entity.enums.GitEventType;
import com.zj.pipeline.entity.vo.GitPushResult;
import com.zj.pipeline.git.RepositoryFactory;
import com.zj.pipeline.git.hook.IGitWebhook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService {

    private final PipelineService pipelineService;
    private final RepositoryFactory repositoryFactory;
    private final UniqueIdService uniqueIdService;
    private final IBindBranchRepository gitBindRepository;
    private final ICodeChangeRepository codeChangeRepository;
    private final Map<String, IGitWebhook> webhookMap;
    private final IDemandRepository demandRepository;
    private final IBugRepository bugRepository;

    public GitBindService(PipelineService pipelineService, RepositoryFactory repositoryFactory,
                          UniqueIdService uniqueIdService, IBindBranchRepository gitBindRepository,
                          ICodeChangeRepository codeChangeRepository, List<IGitWebhook> webhooks, IDemandRepository demandRepository, IBugRepository bugRepository) {
        this.pipelineService = pipelineService;
        this.repositoryFactory = repositoryFactory;
        this.uniqueIdService = uniqueIdService;
        this.gitBindRepository = gitBindRepository;
        this.codeChangeRepository = codeChangeRepository;
        webhookMap = webhooks.stream().collect(Collectors.toMap(IGitWebhook::platform, webhook -> webhook));
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
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

    public boolean notifyHook(Object data, String platform) {
        IGitWebhook gitWebhook = webhookMap.get(platform);
        GitPushResult gitPushResult = gitWebhook.webhook(data);
        updateProcessStatusIfNeed(gitPushResult);
        return true;
    }

    private void updateProcessStatusIfNeed(GitPushResult gitPushResult) {
        if (Objects.nonNull(gitPushResult) && Objects.equals(gitPushResult.getEventType(), GitEventType.COMMIT.getType())) {
            List<CodeChangeBO> codeChanges = codeChangeRepository.getServiceChanges(gitPushResult.getRelatedServiceId());
            List<String> demandCodeChanges = codeChanges.stream().filter(codeChange ->
                    Objects.equals(RelationType.DEMAND.getType(), codeChange.getRelationType()))
                    .map(CodeChangeBO::getRelationId).collect(Collectors.toList());
            boolean demandProcessing = demandRepository.batchUpdateProcessing(demandCodeChanges);
            log.info("update demand processing result={}", demandProcessing);
            List<String> bugCodeChanges = codeChanges.stream().filter(codeChange ->
                            Objects.equals(RelationType.BUG.getType(), codeChange.getRelationType()))
                    .map(CodeChangeBO::getRelationId).collect(Collectors.toList());
            boolean updateBugProcessing = bugRepository.batchUpdateProcessing(bugCodeChanges);
            log.info("update bug processing result={}", updateBugProcessing);
        }
    }


    public List<String> getServiceBranch(String serviceId) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        return repository.listBranch(gitAccessInfo);
    }

    public List<CommitMessage> getBranchCommits(String serviceId, String branchName) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        return repository.getBranchCommits(branchName, gitAccessInfo);
    }
}
