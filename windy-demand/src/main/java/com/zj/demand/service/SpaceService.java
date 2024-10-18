package com.zj.demand.service;

import com.zj.common.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.IterationDTO;
import com.zj.domain.entity.dto.demand.SpaceDTO;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.ISpaceRepository;
import com.zj.domain.repository.demand.IterationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SpaceService {

    private final ISpaceRepository spaceRepository;
    private final UniqueIdService uniqueIdService;
    private final IDemandRepository demandRepository;
    private final IBugRepository bugRepository;
    private final IterationRepository iterationRepository;
    private final IAuthService authService;

    public SpaceService(ISpaceRepository spaceRepository, UniqueIdService uniqueIdService,
                        IDemandRepository demandRepository, IBugRepository bugRepository,
                        IterationRepository iterationRepository, IAuthService authService) {
        this.spaceRepository = spaceRepository;
        this.uniqueIdService = uniqueIdService;
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.iterationRepository = iterationRepository;
        this.authService = authService;
    }

    public List<SpaceDTO> getSpaceList() {
        return spaceRepository.getSpaceList();
    }

    public SpaceDTO createSpace(SpaceDTO spaceDTO) {
        spaceDTO.setUserId(authService.getCurrentUserId());
        spaceDTO.setSpaceId(uniqueIdService.getUniqueId());
        return spaceRepository.createSpace(spaceDTO);
    }

    public boolean updateSpace(String spaceId, SpaceDTO spaceDTO) {
        SpaceDTO space = spaceRepository.getSpace(spaceId);
        if (Objects.isNull(space)) {
            log.info("space not exist = {}", spaceId);
            throw new ApiException(ErrorCode.SPACE_NOT_EXIST);
        }
        spaceDTO.setSpaceId(spaceId);
        return spaceRepository.updateSpace(spaceDTO);
    }

    public boolean deleteSpace(String spaceId) {
        List<IterationDTO> spaceIterationList = iterationRepository.getSpaceNotHandleIterations(spaceId);
        if (CollectionUtils.isNotEmpty(spaceIterationList)) {
            log.info("space has iterations can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_ITERATION);
        }

        List<DemandDTO> notHandleDemands = demandRepository.getSpaceNotHandleDemands(spaceId);
        if (CollectionUtils.isNotEmpty(notHandleDemands)) {
            log.info("space has demands can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_DEMAND);
        }

        bugRepository.getSpaceNotHandleBugs(spaceId);
        if (CollectionUtils.isNotEmpty(notHandleDemands)) {
            log.info("space has bugs can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_BUG);
        }
        return spaceRepository.deleteSpace(spaceId);
    }

    public SpaceDTO getSpace(String spaceId) {
        return spaceRepository.getSpace(spaceId);
    }
}
