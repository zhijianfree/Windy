package com.zj.demand.service;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.DemandQuery;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DemandService {

    private final IAuthService authService;
    private final IDemandRepository demandRepository;
    private final UniqueIdService uniqueIdService;
    private final IBusinessStatusRepository businessStatusRepository;

    public DemandService(IAuthService authService, IDemandRepository demandRepository,
                         UniqueIdService uniqueIdService, IBusinessStatusRepository businessStatusRepository) {
        this.authService = authService;
        this.demandRepository = demandRepository;
        this.uniqueIdService = uniqueIdService;
        this.businessStatusRepository = businessStatusRepository;
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

    public PageSize<DemandDTO> getDemandPage(Integer page, Integer size, String name, Integer status) {
        String currentUserId = authService.getCurrentUserId();
        DemandQuery demandQuery =
                DemandQuery.builder().pageSize(size).page(page).name(name).status(status).creator(currentUserId).build();
        return demandRepository.getDemandPage(demandQuery);
    }

    public PageSize<DemandDTO> getUserDemands(Integer page, Integer size, Integer status) {
        String currentUserId = authService.getCurrentUserId();
        DemandQuery demandQuery =
                DemandQuery.builder().pageSize(size).page(page).status(status).creator(currentUserId).build();
        return demandRepository.getDemandPage(demandQuery);
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

    public List<BusinessStatusDTO> getDemandStatuses() {
        return businessStatusRepository.getDemandStatuses();
    }


}
