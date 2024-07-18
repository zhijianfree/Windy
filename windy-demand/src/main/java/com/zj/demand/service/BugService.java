package com.zj.demand.service;

import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.repository.demand.IBugRepository;
import org.springframework.stereotype.Service;

@Service
public class BugService {

    private final IBugRepository bugRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;

    public BugService(IBugRepository bugRepository, UniqueIdService uniqueIdService, IAuthService authService) {
        this.bugRepository = bugRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
    }

    public BugDTO createBug(BugDTO bugDTO) {
        return null;
    }

    public Boolean updateBug(BugDTO bugDTO) {
        return null;
    }

    public PageSize<BugDTO> getBugPage(Integer page, Integer size) {
        return null;
    }

    public BugDTO getBug(String bugId) {
        return null;
    }

    public Boolean deleteBug(String bugId) {
        return null;
    }

    public PageSize<BugDTO> getRelatedBugs(Integer page, Integer size) {
        return null;
    }
}
