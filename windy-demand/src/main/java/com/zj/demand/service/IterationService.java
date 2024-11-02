package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.demand.entity.IterationStatistic;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.IterationBO;
import com.zj.domain.entity.bo.service.ResourceMemberDto;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.IMemberRepository;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import com.zj.domain.repository.demand.IterationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IterationService {

    private final IDemandRepository demandRepository;
    private final IBugRepository bugRepository;
    private final IWorkTaskRepository workTaskRepository;
    private final IterationRepository iterationRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;
    private final IMemberRepository memberRepository;
    private final IBusinessStatusRepository businessStatusRepository;
    public IterationService(IDemandRepository demandRepository, IBugRepository bugRepository,
                            IWorkTaskRepository workTaskRepository, IterationRepository iterationRepository,
                            UniqueIdService uniqueIdService, IAuthService authService, IMemberRepository memberRepository, IBusinessStatusRepository businessStatusRepository) {
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.workTaskRepository = workTaskRepository;
        this.iterationRepository = iterationRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.memberRepository = memberRepository;
        this.businessStatusRepository = businessStatusRepository;
    }

    public List<IterationBO> getSpaceIterationList(String spaceId) {
        String currentUserId = authService.getCurrentUserId();
        List<ResourceMember> resourceMembers = memberRepository.getResourceMembersByUser(currentUserId);
        if (CollectionUtils.isEmpty(resourceMembers)) {
            return Collections.emptyList();
        }
        List<String> iterationIds = resourceMembers.stream().map(ResourceMember::getResourceId).collect(Collectors.toList());
        return iterationRepository.getIterationList(spaceId, iterationIds);
    }

    public IterationBO createIteration(IterationBO iterationBO) {
        iterationBO.setIterationId(uniqueIdService.getUniqueId());
        iterationBO.setUserId(authService.getCurrentUserId());
        return iterationRepository.createIteration(iterationBO);
    }

    public Boolean updateIteration(String iterationId, IterationBO iterationBO) {
        IterationBO iteration = getIteration(iterationId);
        if (Objects.isNull(iteration)) {
            log.info("iteration is not exist={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_NOT_EXIST);
        }

        if(Objects.nonNull(iterationBO.getStatus()) && iterationBO.getStatus() < iteration.getStatus()){
            log.info("iteration status update error status= {}", iterationBO.getStatus());
            throw new ApiException(ErrorCode.UPDATE_ITERATION_STATUS_ERROR);
        }
        return iterationRepository.updateIteration(iterationBO);
    }

    public boolean deleteIteration(String iterationId) {
        List<BugBO> notHandleBugs = bugRepository.getIterationNotHandleBugs(iterationId);
        if (CollectionUtils.isNotEmpty(notHandleBugs)) {
            log.info("iteration has bugs can not delete iteration={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_HAS_NOT_COMPLETE_BUG);
        }
        List<DemandBO> notHandleDemands = demandRepository.getIterationNotHandleDemands(iterationId);
        if (CollectionUtils.isNotEmpty(notHandleDemands)) {
            log.info("iteration has demands can not delete iteration={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_HAS_NOT_COMPLETE_DEMAND);
        }
        return iterationRepository.deleteIteration(iterationId);
    }

    public IterationBO getIteration(String iterationId) {
        return iterationRepository.getIteration(iterationId);
    }

    public IterationStatistic getIterationStatistic(String iterationId) {
        Integer demandCount = demandRepository.countIteration(iterationId);
        Integer bugCount = bugRepository.countIteration(iterationId);
        Integer workCount = workTaskRepository.countIteration(iterationId);
        return new IterationStatistic(demandCount, bugCount, workCount, 0);
    }

    public List<UserBO> queryIterationMembers(String iterationId) {
        return memberRepository.queryResourceMembers(iterationId);
    }

    public Boolean addIterationMember(ResourceMemberDto resourceMemberDto) {
        return memberRepository.addResourceMember(resourceMemberDto);
    }

    public Boolean deleteIterationMember(String iterationId, String userId) {
        return memberRepository.deleteResourceMember(iterationId, userId);
    }

    public List<BusinessStatusBO> getIterationStatuses() {
        return businessStatusRepository.getIterationStatuses();
    }
}
