package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;

import java.util.List;

public interface IDemandRepository {
    boolean createDemand(DemandBO demand);

    PageSize<DemandBO> getDemandPage(DemandQueryBO demandQueryBO);

    boolean updateDemand(DemandBO demand);

    DemandBO getDemand(String demandId);

    boolean deleteDemand(String demandId);

    PageSize<DemandBO> getRelatedDemands(String proposer, Integer page, Integer size);

    Integer countIteration(String iterationId);

    List<DemandBO> getIterationDemand(String iterationId);

    List<DemandBO> getDemandsByName(String queryName);

    List<DemandBO> getSpaceNotHandleDemands(String spaceId);

    List<DemandBO> getIterationNotHandleDemands(String iterationId);
}
