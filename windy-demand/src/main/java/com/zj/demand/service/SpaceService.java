package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.SpaceDto;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.IterationBO;
import com.zj.domain.entity.bo.demand.SpaceBO;
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

    public List<SpaceBO> getSpaceList() {
        return spaceRepository.getSpaceList();
    }

    public SpaceBO createSpace(SpaceDto spaceDto) {
        SpaceBO spaceBO = OrikaUtil.convert(spaceDto, SpaceBO.class);
        spaceBO.setUserId(authService.getCurrentUserId());
        spaceBO.setSpaceId(uniqueIdService.getUniqueId());
        return spaceRepository.createSpace(spaceBO);
    }

    public boolean updateSpace(String spaceId, SpaceDto spaceDto) {
        SpaceBO space = spaceRepository.getSpace(spaceId);
        if (Objects.isNull(space)) {
            log.info("space not exist = {}", spaceId);
            throw new ApiException(ErrorCode.SPACE_NOT_EXIST);
        }
        SpaceBO spaceBO = OrikaUtil.convert(spaceDto, SpaceBO.class);
        spaceBO.setSpaceId(spaceId);
        return spaceRepository.updateSpace(spaceBO);
    }

    public boolean deleteSpace(String spaceId) {
        List<IterationBO> spaceIterationList = iterationRepository.getSpaceNotHandleIterations(spaceId);
        if (CollectionUtils.isNotEmpty(spaceIterationList)) {
            log.info("space has iterations can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_ITERATION);
        }

        List<DemandBO> notHandleDemands = demandRepository.getSpaceNotHandleDemands(spaceId);
        if (CollectionUtils.isNotEmpty(notHandleDemands)) {
            log.info("space has demands can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_DEMAND);
        }

        List<BugBO> notHandleBugs = bugRepository.getSpaceNotHandleBugs(spaceId);
        if (CollectionUtils.isNotEmpty(notHandleBugs)) {
            log.info("space has bugs can not delete spaceId={}", spaceId);
            throw new ApiException(ErrorCode.SPACE_HAS_NOT_COMPLETE_BUG);
        }
        return spaceRepository.deleteSpace(spaceId);
    }

    public SpaceBO getSpace(String spaceId) {
        return spaceRepository.getSpace(spaceId);
    }
}
