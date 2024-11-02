package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.demand.entity.BugDetail;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
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

    public BugBO createBug(BugBO bugBO) {
        bugBO.setBugId(uniqueIdService.getUniqueId());
        UserDetail userDetail = authService.getUserDetail();
        bugBO.setProposer(userDetail.getUserId());
        bugBO.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        return bugRepository.createBug(bugBO) ? bugBO : null;
    }

    public Boolean updateBug(BugBO bugBO) {
        return bugRepository.updateBug(bugBO);
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

    public BugDetail getBug(String bugId) {
        BugBO bug = bugRepository.getBug(bugId);
        if (Objects.isNull(bug)) {
            log.info("bug detail not find ={}", bugId);
            throw new ApiException(ErrorCode.BUG_NOT_EXIST);
        }
        BugDetail bugDetail = OrikaUtil.convert(bug, BugDetail.class);
        UserBO proposer = userRepository.getUserByUserId(bug.getProposer());
        Optional.ofNullable(proposer).ifPresent(p -> bugDetail.setProposerName(p.getUserName()));
        UserBO acceptor = userRepository.getUserByUserId(bug.getAcceptor());
        Optional.ofNullable(acceptor).ifPresent(a -> bugDetail.setAcceptorName(a.getUserName()));
        return bugDetail;
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
