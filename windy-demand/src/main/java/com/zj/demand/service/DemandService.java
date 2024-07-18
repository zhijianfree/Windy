package com.zj.demand.service;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.repository.demand.IDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DemandService {

    private final IAuthService authService;
    private final IDemandRepository demandRepository;
    private final UniqueIdService uniqueIdService;

    public DemandService(IAuthService authService, IDemandRepository demandRepository, UniqueIdService uniqueIdService) {
        this.authService = authService;
        this.demandRepository = demandRepository;
        this.uniqueIdService = uniqueIdService;
    }

    public DemandDTO createDemand(DemandDTO demandDTO) {
        demandDTO.setDemandId(uniqueIdService.getUniqueId());
        demandDTO.setCreator(authService.getCurrentUserId());
        boolean result = demandRepository.createDemand(demandDTO);
        if (!result) {
            log.info("create demand error ={}", demandDTO.getDemandName());
            throw new ApiException(ErrorCode.DEMAND_CREATE_ERROR);
        }
        return demandDTO;
    }

    public PageSize<DemandDTO> getDemandPage(Integer page, Integer size) {
        String currentUserId = authService.getCurrentUserId();
        return demandRepository.getDemandPage(currentUserId, page, size);
    }

    public boolean updateDemand(DemandDTO demandDTO) {
        return demandRepository.updateDemand(demandDTO);
    }

    public DemandDTO getDemand(String demandId) {
        return demandRepository.getDemand(demandId);
    }

    public Boolean deleteDemand(String demandId) {
        return demandRepository.deleteDemand(demandId);
    }

    public PageSize<DemandDTO> getRelatedDemands(Integer page, Integer size) {
        String currentUserId = authService.getCurrentUserId();
        return demandRepository.getRelatedDemands(currentUserId, page, size);
    }
}
