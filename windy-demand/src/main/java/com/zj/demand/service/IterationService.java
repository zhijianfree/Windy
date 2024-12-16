package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.IterationDto;
import com.zj.demand.entity.IterationMemberDto;
import com.zj.demand.entity.IterationStatisticDto;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.IterationBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.entity.enums.MemberType;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.IMemberRepository;
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
    private final IterationRepository iterationRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;
    private final IMemberRepository memberRepository;
    private final IBusinessStatusRepository businessStatusRepository;

    public IterationService(IDemandRepository demandRepository, IBugRepository bugRepository,
                            IterationRepository iterationRepository,
                            UniqueIdService uniqueIdService, IAuthService authService,
                            IMemberRepository memberRepository, IBusinessStatusRepository businessStatusRepository) {
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.iterationRepository = iterationRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.memberRepository = memberRepository;
        this.businessStatusRepository = businessStatusRepository;
    }

    public List<IterationBO> getSpaceIterationList(String spaceId) {
        String currentUserId = authService.getCurrentUserId();
        List<ResourceMemberBO> resourceMembers = memberRepository.getByRelationMember(currentUserId,
                MemberType.ITERATION_MEMBER.getType());
        if (CollectionUtils.isEmpty(resourceMembers)) {
            return Collections.emptyList();
        }
        List<String> iterationIds =
                resourceMembers.stream().map(ResourceMemberBO::getResourceId).collect(Collectors.toList());
        return iterationRepository.getIterationList(spaceId, iterationIds);
    }

    public IterationBO createIteration(IterationDto iterationDto) {
        IterationBO iterationBO = OrikaUtil.convert(iterationDto, IterationBO.class);
        iterationBO.setIterationId(uniqueIdService.getUniqueId());
        String currentUserId = authService.getCurrentUserId();
        iterationBO.setUserId(currentUserId);
        IterationBO iteration = iterationRepository.createIteration(iterationBO);
        if (Objects.nonNull(iteration)) {
            ResourceMemberBO resourceMemberBO = new ResourceMemberBO();
            resourceMemberBO.setMemberType(MemberType.ITERATION_MEMBER.getType());
            resourceMemberBO.setRelationId(currentUserId);
            resourceMemberBO.setResourceId(iteration.getIterationId());
            boolean addResourceMember = memberRepository.addResourceMember(resourceMemberBO);
            log.info("add default iteration member result={}", addResourceMember);
        }
        return iteration;
    }

    public Boolean updateIteration(String iterationId, IterationDto iterationDto) {
        IterationBO iteration = getIteration(iterationId);
        if (Objects.isNull(iteration)) {
            log.info("iteration is not exist={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_NOT_EXIST);
        }

        boolean unchangeableStatus = businessStatusRepository.isUnchangeableStatus(iteration.getStatus(),
                BusinessStatusType.ITERATION.name());
        if (Objects.nonNull(iterationDto.getStatus()) && unchangeableStatus) {
            log.info("iteration status is unchangeable status= {}", iterationDto.getStatus());
            throw new ApiException(ErrorCode.STATUS_UNCHANGEABLE_ERROR);
        }
        return iterationRepository.updateIteration(OrikaUtil.convert(iterationDto, IterationBO.class));
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

    public IterationStatisticDto getIterationStatistic(String iterationId) {
        List<DemandBO> iterationDemand = demandRepository.getIterationDemand(iterationId);
        double demandWorkload =
                iterationDemand.stream().filter(Objects::nonNull).map(DemandBO::getWorkload).mapToInt(Integer::intValue).sum();
        List<BugBO> iterationBugs = bugRepository.getIterationBugs(iterationId);
        double bugWorkload =
                iterationBugs.stream().filter(Objects::nonNull).map(BugBO::getWorkload).mapToDouble(Integer::intValue).sum();
        return new IterationStatisticDto(iterationDemand.size(), iterationBugs.size(), 0,
                (int) (demandWorkload + bugWorkload));
    }

    public List<UserBO> queryIterationMembers(String iterationId) {
        return memberRepository.getResourceUserMembers(iterationId, MemberType.ITERATION_MEMBER.getType());
    }

    public Boolean addIterationMember(IterationMemberDto memberDto) {
        ResourceMemberBO resourceMemberBO = new ResourceMemberBO();
        resourceMemberBO.setMemberType(MemberType.ITERATION_MEMBER.getType());
        resourceMemberBO.setResourceId(memberDto.getIterationId());
        resourceMemberBO.setRelationId(memberDto.getUserId());
        return memberRepository.addResourceMember(resourceMemberBO);
    }

    public Boolean deleteIterationMember(String iterationId, String userId) {
        return memberRepository.deleteResourceMember(iterationId, userId);
    }

    public List<BusinessStatusBO> getIterationStatuses() {
        return businessStatusRepository.getIterationStatuses();
    }
}
