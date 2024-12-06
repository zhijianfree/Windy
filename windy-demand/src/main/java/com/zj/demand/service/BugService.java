package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.demand.entity.BugDetailDto;
import com.zj.demand.entity.BugDto;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import lombok.extern.slf4j.Slf4j;
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
    private final IBusinessStatusRepository businessStatusRepository;

    public BugService(IBugRepository bugRepository, UniqueIdService uniqueIdService, IAuthService authService,
                      IUserRepository userRepository, IBusinessStatusRepository businessStatusRepository) {
        this.bugRepository = bugRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.businessStatusRepository = businessStatusRepository;
    }

    public BugDto createBug(BugDto bugDto) {
        bugDto.setBugId(uniqueIdService.getUniqueId());
        UserDetail userDetail = authService.getUserDetail();
        bugDto.setProposer(userDetail.getUserId());
        bugDto.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        BugBO bugBO = OrikaUtil.convert(bugDto, BugBO.class);
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

    public PageSize<BugBO> getBugPage(Integer page, Integer size, String name, Integer status, String spaceId, String iterationId) {
        String userId = authService.getCurrentUserId();
        BugQueryBO bugQueryBO = BugQueryBO.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .name(name)
                .iterationId(iterationId)
                .status(status)
                .spaceId(spaceId).build();
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
        return bugRepository.deleteBug(bugId);
    }

    public PageSize<BugBO> getRelatedBugs(Integer page, Integer size, Integer status) {
        String userId = authService.getCurrentUserId();
        BugQueryBO bugQueryBO = BugQueryBO.builder().page(page).size(size).userId(userId).status(status).build();
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
