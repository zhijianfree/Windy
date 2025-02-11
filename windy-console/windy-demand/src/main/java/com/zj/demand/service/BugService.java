package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.BugDetailDto;
import com.zj.demand.entity.BugDto;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class BugService {

    private final IBugRepository bugRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;
    private final IUserRepository userRepository;
    private final ICodeChangeRepository codeChangeRepository;
    private final IBusinessStatusRepository businessStatusRepository;
    private final IterationService iterationService;

    public BugService(IBugRepository bugRepository, UniqueIdService uniqueIdService, IAuthService authService,
                      IUserRepository userRepository, ICodeChangeRepository codeChangeRepository, IBusinessStatusRepository businessStatusRepository, IterationService iterationService) {
        this.bugRepository = bugRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.codeChangeRepository = codeChangeRepository;
        this.businessStatusRepository = businessStatusRepository;
        this.iterationService = iterationService;
    }

    public BugDto createBug(BugDto bugDto) {
        iterationService.checkIterationUnchangeable(bugDto.getIterationId());
        bugDto.setBugId(uniqueIdService.getUniqueId());
        BugBO bugBO = OrikaUtil.convert(bugDto, BugBO.class);
        UserDetail userDetail = authService.getUserDetail();
        bugBO.setProposer(userDetail.getUserId());
        bugBO.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        return bugRepository.createBug(bugBO) ? bugDto : null;
    }

    public Boolean updateBug(BugDto bugDto) {
        BugBO bug = bugRepository.getBug(bugDto.getBugId());
        boolean unchangeableStatus = businessStatusRepository.isUnchangeableStatus(bug.getStatus(),
                BusinessStatusType.BUG.name());
        if (Objects.nonNull(bugDto.getStatus()) && unchangeableStatus) {
            log.info("demand status is unchangeable status= {}", bug.getStatus());
            throw new ApiException(ErrorCode.STATUS_UNCHANGEABLE_ERROR);
        }
        return bugRepository.updateBug(OrikaUtil.convert(bugDto, BugBO.class));
    }

    public PageSize<BugBO> getBugPage(Integer page, Integer size, String name, Integer status, String spaceId,
                                      String iterationId, String acceptor, Integer type) {
        String userId = authService.getCurrentUserId();
        BugQueryBO bugQueryBO = BugQueryBO.builder().page(page).size(size).name(name).proposer(userId)
                .acceptor(acceptor).iterationId(iterationId).status(status).spaceId(spaceId).build();
        bugQueryBO.handleQueryType(type, userId);
        return bugRepository.getUserBugs(bugQueryBO);
    }

    public BugDetailDto getBug(String bugId) {
        BugBO bug = bugRepository.getBug(bugId);
        if (Objects.isNull(bug)) {
            log.info("bug detail not find ={}", bugId);
            throw new ApiException(ErrorCode.BUG_NOT_EXIST);
        }
        BugDetailDto bugDetailDto = OrikaUtil.convert(bug, BugDetailDto.class);
        UserBO proposer = userRepository.getUserByUserId(bug.getProposer());
        Optional.ofNullable(proposer).ifPresent(p -> bugDetailDto.setProposerName(p.getUserName()));
        UserBO acceptor = userRepository.getUserByUserId(bug.getAcceptor());
        Optional.ofNullable(acceptor).ifPresent(a -> bugDetailDto.setAcceptorName(a.getUserName()));
        return bugDetailDto;
    }

    public Boolean deleteBug(String bugId) {
        BugBO bug = bugRepository.getBug(bugId);
        if (Objects.isNull(bug)) {
            log.info("bug not find bugId={}", bugId);
            throw new ApiException(ErrorCode.BUG_NOT_EXIST);
        }

        boolean completeStatus = businessStatusRepository.isUnchangeableStatus(bug.getStatus(), BusinessStatusType.BUG.name());
        List<CodeChangeBO> codeChanges = codeChangeRepository.getCodeChangeByRelationId(bugId,
                RelationType.BUG.getType());
        if (CollectionUtils.isNotEmpty(codeChanges) && !completeStatus) {
            log.info("bug has bind branch bugId={}", bugId);
            throw new ApiException(ErrorCode.BUG_HAS_BIND_BRANCH);
        }
        return bugRepository.deleteBug(bugId);
    }

    public PageSize<BugBO> getUserRelatedBugs(Integer page, Integer size, Integer status) {
        String userId = authService.getCurrentUserId();
        BugQueryBO bugQueryBO = BugQueryBO.builder().page(page).size(size).acceptor(userId).status(status).build();
        return bugRepository.getUserRelatedBugs(bugQueryBO);
    }

    public List<BusinessStatusBO> getBugStatuses() {
        return businessStatusRepository.getBugStatuses();
    }

    public List<BugBO> getIterationBugs(String iterationId) {
        return bugRepository.getIterationBugs(iterationId);
    }

    public List<BusinessStatusBO> getBugTags() {
        return businessStatusRepository.getBugTags();
    }
}
