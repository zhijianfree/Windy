package com.zj.demand.service;

import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.BugQuery;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BugService {

    private final IBugRepository bugRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;
    private final IBusinessStatusRepository businessStatusRepository;

    public BugService(IBugRepository bugRepository, UniqueIdService uniqueIdService, IAuthService authService,
                      IBusinessStatusRepository businessStatusRepository) {
        this.bugRepository = bugRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.businessStatusRepository = businessStatusRepository;
    }

    public BugDTO createBug(BugDTO bugDTO) {
        bugDTO.setBugId(uniqueIdService.getUniqueId());
        bugDTO.setProposer(authService.getCurrentUserId());
        boolean result = bugRepository.createBug(bugDTO);
        return result ? bugDTO : null;
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

    public BugDTO getBug(String bugId) {
        return bugRepository.getBug(bugId);
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
