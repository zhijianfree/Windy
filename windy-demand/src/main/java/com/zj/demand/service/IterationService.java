package com.zj.demand.service;

import com.zj.common.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.demand.entity.IterationStatistic;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.IterationDTO;
import com.zj.domain.entity.dto.service.ResourceMemberDto;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.repository.demand.IBugRepository;
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
    public IterationService(IDemandRepository demandRepository, IBugRepository bugRepository,
                            IWorkTaskRepository workTaskRepository, IterationRepository iterationRepository,
                            UniqueIdService uniqueIdService, IAuthService authService, IMemberRepository memberRepository) {
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.workTaskRepository = workTaskRepository;
        this.iterationRepository = iterationRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.memberRepository = memberRepository;
    }

    public List<IterationDTO> getSpaceIterationList(String spaceId) {
        String currentUserId = authService.getCurrentUserId();
        List<ResourceMember> resourceMembers = memberRepository.getResourceMembersByUser(currentUserId);
        if (CollectionUtils.isEmpty(resourceMembers)) {
            return Collections.emptyList();
        }
        List<String> iterationIds = resourceMembers.stream().map(ResourceMember::getResourceId).collect(Collectors.toList());
        return iterationRepository.getIterationList(spaceId, iterationIds);
    }

    public IterationDTO createIteration(IterationDTO iterationDTO) {
        iterationDTO.setIterationId(uniqueIdService.getUniqueId());
        iterationDTO.setUserId(authService.getCurrentUserId());
        return iterationRepository.createIteration(iterationDTO);
    }

    public Boolean updateIteration(String iterationId, IterationDTO iterationDTO) {
        IterationDTO iteration = getIteration(iterationId);
        if (Objects.isNull(iteration)) {
            log.info("iteration is not exist={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_NOT_EXIST);
        }
        return iterationRepository.updateIteration(iterationDTO);
    }

    public boolean deleteIteration(String iterationId) {
        List<BugDTO> notHandleBugs = bugRepository.getIterationNotHandleBugs(iterationId);
        if (CollectionUtils.isNotEmpty(notHandleBugs)) {
            log.info("iteration has bugs can not delete iteration={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_HAS_NOT_COMPLETE_BUG);
        }
        List<DemandDTO> notHandleDemands = demandRepository.getIterationNotHandleDemands(iterationId);
        if (CollectionUtils.isNotEmpty(notHandleDemands)) {
            log.info("iteration has demands can not delete iteration={}", iterationId);
            throw new ApiException(ErrorCode.ITERATION_HAS_NOT_COMPLETE_DEMAND);
        }
        return iterationRepository.deleteIteration(iterationId);
    }

    public IterationDTO getIteration(String iterationId) {
        return iterationRepository.getIteration(iterationId);
    }

    public IterationStatistic getIterationStatistic(String iterationId) {
        Integer demandCount = demandRepository.countIteration(iterationId);
        Integer bugCount = bugRepository.countIteration(iterationId);
        Integer workCount = workTaskRepository.countIteration(iterationId);
        return new IterationStatistic(demandCount, bugCount, workCount, 0);
    }

    public List<UserDto> queryIterationMembers(String iterationId) {
        return memberRepository.queryResourceMembers(iterationId);
    }

    public Boolean addIterationMember(ResourceMemberDto resourceMemberDto) {
        return memberRepository.addResourceMember(resourceMemberDto);
    }

    public Boolean deleteIterationMember(String iterationId, String userId) {
        return memberRepository.deleteResourceMember(iterationId, userId);
    }
}
