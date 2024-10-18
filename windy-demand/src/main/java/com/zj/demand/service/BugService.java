package com.zj.demand.service;

import com.zj.common.auth.IAuthService;
import com.zj.common.auth.UserDetail;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.demand.entity.BugDetail;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.BugQuery;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
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

    public BugDTO createBug(BugDTO bugDTO) {
        bugDTO.setBugId(uniqueIdService.getUniqueId());
        UserDetail userDetail = authService.getUserDetail();
        bugDTO.setProposer(userDetail.getUserId());
        bugDTO.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        return bugRepository.createBug(bugDTO) ? bugDTO : null;
    }

    public Boolean updateBug(BugDTO bugDTO) {
        return bugRepository.updateBug(bugDTO);
    }

    public PageSize<BugDTO> getBugPage(Integer page, Integer size, String name, Integer status, String spaceId, String iterationId) {
        String userId = authService.getCurrentUserId();
        BugQuery bugQuery = BugQuery.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .name(name)
                .iterationId(iterationId)
                .status(status)
                .spaceId(spaceId).build();
        return bugRepository.getUserBugs(bugQuery);
    }

    public BugDetail getBug(String bugId) {
        BugDTO bug = bugRepository.getBug(bugId);
        if (Objects.isNull(bug)) {
            log.info("bug detail not find ={}", bugId);
            throw new ApiException(ErrorCode.BUG_NOT_EXIST);
        }
        BugDetail bugDetail = OrikaUtil.convert(bug, BugDetail.class);
        UserDto proposer = userRepository.getUserByUserId(bug.getProposer());
        Optional.ofNullable(proposer).ifPresent(p -> bugDetail.setProposerName(p.getUserName()));
        UserDto acceptor = userRepository.getUserByUserId(bug.getAcceptor());
        Optional.ofNullable(acceptor).ifPresent(a -> bugDetail.setAcceptorName(a.getUserName()));
        return bugDetail;
    }

    public Boolean deleteBug(String bugId) {
        return bugRepository.deleteBug(bugId);
    }

    public PageSize<BugDTO> getRelatedBugs(Integer page, Integer size, Integer status) {
        String userId = authService.getCurrentUserId();
        BugQuery bugQuery = BugQuery.builder().page(page).size(size).userId(userId).status(status).build();
        return bugRepository.getUserRelatedBugs(bugQuery);
    }

    public List<BusinessStatusDTO> getBugStatuses() {
        return businessStatusRepository.getBugStatuses();
    }

    public List<BugDTO> getIterationBugs(String iterationId) {
        return bugRepository.getIterationBugs(iterationId);
    }
}
