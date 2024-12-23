package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.DemandDetailDto;
import com.zj.demand.entity.DemandDto;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.enums.BusinessStatusType;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private final ICodeChangeRepository codeChangeRepository;

    public DemandService(IAuthService authService, IDemandRepository demandRepository,
                         UniqueIdService uniqueIdService, IUserRepository userRepository, IBusinessStatusRepository businessStatusRepository, ICodeChangeRepository codeChangeRepository) {
        this.authService = authService;
        this.demandRepository = demandRepository;
        this.uniqueIdService = uniqueIdService;
        this.userRepository = userRepository;
        this.businessStatusRepository = businessStatusRepository;
        this.codeChangeRepository = codeChangeRepository;
    }

    public DemandBO createDemand(DemandDto demandDto) {
        DemandBO demandBO = OrikaUtil.convert(demandDto, DemandBO.class);
        demandBO.setDemandId(uniqueIdService.getUniqueId());
        UserDetail userDetail = authService.getUserDetail();
        demandBO.setProposer(userDetail.getUserId());
        demandBO.setProposerName(Optional.ofNullable(userDetail.getNickName()).orElse(userDetail.getUserName()));
        boolean result = demandRepository.createDemand(demandBO);
        if (!result) {
            log.info("create demand error ={}", demandBO.getDemandName());
            throw new ApiException(ErrorCode.DEMAND_CREATE_ERROR);
        }
        return demandBO;
    }

    public PageSize<DemandBO> getDemandPage(Integer page, Integer size, String name, Integer status, String spaceId, String iterationId, String acceptor, Integer type) {
        String currentUserId = authService.getCurrentUserId();
        DemandQueryBO demandQueryBO = DemandQueryBO.builder().pageSize(size).page(page).name(name).status(status)
                .spaceId(spaceId).acceptor(acceptor).iterationId(iterationId).proposer(currentUserId).build();
        demandQueryBO.handleQueryType(type, currentUserId);
        return demandRepository.getDemandPage(demandQueryBO);
    }

    public PageSize<DemandBO> getUserDemands(Integer page, Integer size, Integer status) {
        String currentUserId = authService.getCurrentUserId();
        DemandQueryBO demandQueryBO =
                DemandQueryBO.builder().pageSize(size).page(page).status(status).proposer(currentUserId).build();
        return demandRepository.getDemandPage(demandQueryBO);
    }

    public boolean updateDemand(DemandDto demandDto) {
        DemandBO demand = demandRepository.getDemand(demandDto.getDemandId());
        boolean unchangeableStatus = businessStatusRepository.isUnchangeableStatus(demand.getStatus(),
                BusinessStatusType.DEMAND.name());
        if (Objects.nonNull(demandDto.getStatus()) && unchangeableStatus) {
            log.info("demand status is unchangeable status= {}", demand.getStatus());
            throw new ApiException(ErrorCode.STATUS_UNCHANGEABLE_ERROR);
        }
        return demandRepository.updateDemand(OrikaUtil.convert(demandDto, DemandBO.class));
    }

    public DemandDetailDto getDemand(String demandId) {
        DemandBO demand = demandRepository.getDemand(demandId);
        if (Objects.isNull(demand)) {
            log.info("can not find demand ={}", demandId);
            throw new ApiException(ErrorCode.DEMAND_NOT_EXIST);
        }
        DemandDetailDto demandDetailDto = OrikaUtil.convert(demand, DemandDetailDto.class);
        UserBO proposer = userRepository.getUserByUserId(demand.getProposer());
        Optional.ofNullable(proposer).ifPresent(p -> demandDetailDto.setProposerName(p.getUserName()));
        UserBO acceptor = userRepository.getUserByUserId(demand.getAcceptor());
        Optional.ofNullable(acceptor).ifPresent(a -> demandDetailDto.setAcceptorName(a.getUserName()));
        return demandDetailDto;
    }

    public Boolean deleteDemand(String demandId) {
        DemandBO demand = demandRepository.getDemand(demandId);
        if (Objects.isNull(demand)) {
            log.info("demand not find demandId={}", demandId);
            throw new ApiException(ErrorCode.DEMAND_NOT_EXIST);
        }

        boolean completeStatus = businessStatusRepository.isUnchangeableStatus(demand.getStatus(),
                BusinessStatusType.DEMAND.name());
        List<CodeChangeBO> codeChanges = codeChangeRepository.getCodeChangeByRelationId(demandId, RelationType.DEMAND.getType());
        if (CollectionUtils.isNotEmpty(codeChanges) && !completeStatus) {
            log.info("demand has bind branch demandId={}", demandId);
            throw new ApiException(ErrorCode.DEMAND_HAS_BIND_BRANCH);
        }
        return demandRepository.deleteDemand(demandId);
    }

    public PageSize<DemandBO> getRelatedDemands(Integer page, Integer size) {
        String currentUserId = authService.getCurrentUserId();
        return demandRepository.getRelatedDemands(currentUserId, page, size);
    }

    public List<BusinessStatusBO> getDemandStatuses() {
        return businessStatusRepository.getDemandStatuses();
    }


    public List<DemandBO> getIterationDemands(String iterationId) {
        return demandRepository.getIterationDemand(iterationId);
    }

    public List<BusinessStatusBO> getDemandTags() {
        return businessStatusRepository.getDemandTags();
    }
}
