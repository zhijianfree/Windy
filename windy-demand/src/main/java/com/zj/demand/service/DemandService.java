package com.zj.demand.service;

import com.zj.common.auth.IAuthService;
import com.zj.common.auth.UserDetail;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.demand.entity.DemandDetail;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.demand.BusinessStatusDto;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.DemandQuery;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class DemandService {

    private final IAuthService authService;
    private final IDemandRepository demandRepository;
    private final UniqueIdService uniqueIdService;
    private final IUserRepository userRepository;
    private final IBusinessStatusRepository businessStatusRepository;

    public DemandService(IAuthService authService, IDemandRepository demandRepository,
                         UniqueIdService uniqueIdService, IUserRepository userRepository, IBusinessStatusRepository businessStatusRepository) {
        this.authService = authService;
        this.demandRepository = demandRepository;
        this.uniqueIdService = uniqueIdService;
        this.userRepository = userRepository;
        this.businessStatusRepository = businessStatusRepository;
    }

    public DemandDTO createDemand(DemandDTO demandDTO) {
        demandDTO.setDemandId(uniqueIdService.getUniqueId());
        UserDetail userDetail = authService.getUserDetail();
        demandDTO.setProposer(userDetail.getUserId());
        demandDTO.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        boolean result = demandRepository.createDemand(demandDTO);
        if (!result) {
            log.info("create demand error ={}", demandDTO.getDemandName());
            throw new ApiException(ErrorCode.DEMAND_CREATE_ERROR);
        }
        return demandDTO;
    }

    public PageSize<DemandDTO> getDemandPage(Integer page, Integer size, String name, Integer status, String spaceId, String iterationId) {
        String currentUserId = authService.getCurrentUserId();
        DemandQuery demandQuery = DemandQuery.builder()
                .pageSize(size)
                .page(page)
                .name(name)
                .status(status)
                .spaceId(spaceId)
                .iterationId(iterationId)
                .creator(currentUserId).build();
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

    public DemandDetail getDemand(String demandId) {
        DemandDTO demand = demandRepository.getDemand(demandId);
        if (Objects.isNull(demand)) {
            log.info("can not find demand ={}", demandId);
            throw new ApiException(ErrorCode.DEMAND_NOT_EXIST);
        }
        DemandDetail demandDetail = OrikaUtil.convert(demand, DemandDetail.class);
        UserDto proposer = userRepository.getUserByUserId(demand.getProposer());
        Optional.ofNullable(proposer).ifPresent(p -> demandDetail.setProposerName(p.getUserName()));
        UserDto acceptor = userRepository.getUserByUserId(demand.getAcceptor());
        Optional.ofNullable(acceptor).ifPresent(a -> demandDetail.setAcceptorName(a.getUserName()));
        return demandDetail;
    }

    public Boolean deleteDemand(String demandId) {
        return demandRepository.deleteDemand(demandId);
    }

    public PageSize<DemandDTO> getRelatedDemands(Integer page, Integer size) {
        String currentUserId = authService.getCurrentUserId();
        return demandRepository.getRelatedDemands(currentUserId, page, size);
    }

    public List<BusinessStatusDto> getDemandStatuses() {
        return businessStatusRepository.getDemandStatuses();
    }


    public List<DemandDTO> getIterationDemands(String iterationId) {
        return demandRepository.getIterationDemand(iterationId);
    }

    public List<BusinessStatusDto> getDemandTags() {
        return businessStatusRepository.getDemandTags();
    }
}
