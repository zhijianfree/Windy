package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.RelationDemandBug;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.domain.entity.enums.RelationType;
import com.zj.pipeline.git.RepositoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class CodeChangeService {

    private final RepositoryFactory repositoryFactory;
    private final IMicroServiceRepository serviceRepository;
    private final UniqueIdService uniqueIdService;
    private final ICodeChangeRepository codeChangeRepository;
    private final IBugRepository bugRepository;
    private final IDemandRepository demandRepository;
    private final IWorkTaskRepository workTaskRepository;

    public CodeChangeService(RepositoryFactory repositoryFactory, IMicroServiceRepository serviceRepository,
                             UniqueIdService uniqueIdService, ICodeChangeRepository codeChangeRepository,
                             IBugRepository bugRepository, IDemandRepository demandRepository,
                             IWorkTaskRepository workTaskRepository) {
        this.repositoryFactory = repositoryFactory;
        this.serviceRepository = serviceRepository;
        this.uniqueIdService = uniqueIdService;
        this.codeChangeRepository = codeChangeRepository;
        this.bugRepository = bugRepository;
        this.demandRepository = demandRepository;
        this.workTaskRepository = workTaskRepository;
    }

    public CodeChangeBO getCodeChange(String serviceId, String codeChangeId) {
        Assert.notEmpty(serviceId, "serviceId can not be null");
        return codeChangeRepository.getCodeChange(codeChangeId);
    }

    public String createCodeChange(CodeChangeBO codeChange) {
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(codeChange.getServiceId());
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        repository.createBranch(codeChange.getChangeBranch(), gitAccessInfo);

        codeChange.setChangeId(uniqueIdService.getUniqueId());
        return codeChangeRepository.saveCodeChange(codeChange) ? codeChange.getChangeId() : "";
    }

    public boolean updateCodeChange(String serviceId, String codeChangeId, CodeChangeBO codeChange) {
        CodeChangeBO changeDto = getCodeChange(serviceId, codeChangeId);
        if (Objects.isNull(changeDto)) {
            throw new ApiException(ErrorCode.NOT_FOUND_CODE_CHANGE);
        }

        codeChange.setChangeId(codeChangeId);
        codeChange.setUpdateTime(System.currentTimeMillis());
        return codeChangeRepository.updateCodeChange(codeChange);
    }

    public List<CodeChangeBO> listCodeChanges(String serviceId) {
        checkServiceExist(serviceId);
        return codeChangeRepository.getServiceChanges(serviceId);
    }

    public Boolean deleteCodeChange(String serviceId, String codeChangeId) {
        CodeChangeBO codeChange = getCodeChange(serviceId, codeChangeId);
        GitAccessInfo gitAccessInfo = repositoryFactory.getServiceRepositoryAccessInfo(serviceId);
        IGitRepositoryHandler repository = repositoryFactory.getRepository(gitAccessInfo.getGitType());
        repository.deleteBranch(codeChange.getChangeBranch(), gitAccessInfo);
        return codeChangeRepository.deleteCodeChange(codeChangeId);
    }

    public List<RelationDemandBug> queryRelationIds(String queryName) {
        CompletableFuture<List<BugBO>> bugFuture =
                CompletableFuture.supplyAsync(() -> bugRepository.getBugsFuzzyByName(queryName));
        CompletableFuture<List<DemandBO>> demandFuture =
                CompletableFuture.supplyAsync(() -> demandRepository.getDemandsByFuzzName(queryName));
        CompletableFuture<List<WorkTaskBO>> workFuture =
                CompletableFuture.supplyAsync(() -> workTaskRepository.getWorkTaskByName(queryName));
        CompletableFuture.allOf(bugFuture, demandFuture, workFuture).join();
        try {
            List<RelationDemandBug> relationList =
                    bugFuture.get().stream().map(bug -> new RelationDemandBug(bug.getBugId(),
                    RelationType.BUG.getType(), bug.getBugName())).collect(Collectors.toList());
            List<RelationDemandBug> relationDemands =
                    demandFuture.get().stream().map(demand -> new RelationDemandBug(demand.getDemandId(),
                            RelationType.DEMAND.getType(), demand.getDemandName())).collect(Collectors.toList());
            List<RelationDemandBug> relationWorks =
                    workFuture.get().stream().map(work -> new RelationDemandBug(work.getTaskId(),
                            RelationType.WORK.getType(), work.getTaskName())).collect(Collectors.toList());
            relationList.addAll(relationDemands);
            relationList.addAll(relationWorks);
            return relationList;
        } catch (Exception e) {
            log.info("query related demand or bug or work error", e);
        }
        return Collections.emptyList();
    }

    private void checkServiceExist(String serviceId) {
        MicroserviceBO serviceDetail = serviceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(serviceDetail)) {
            log.warn("can not find serviceId ={}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }
    }
}
