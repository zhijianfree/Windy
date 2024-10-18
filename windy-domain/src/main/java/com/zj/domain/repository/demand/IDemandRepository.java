package com.zj.domain.repository.demand;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.DemandQuery;

import java.util.List;

public interface IDemandRepository {
    boolean createDemand(DemandDTO demand);

    PageSize<DemandDTO> getDemandPage(DemandQuery demandQuery);

    boolean updateDemand(DemandDTO demand);

    DemandDTO getDemand(String demandId);

    boolean deleteDemand(String demandId);

    PageSize<DemandDTO> getRelatedDemands(String proposer, Integer page, Integer size);

    Integer countIteration(String iterationId);

    List<DemandDTO> getIterationDemand(String iterationId);

    List<DemandDTO> getDemandsByName(String queryName);

    List<DemandDTO> getSpaceNotHandleDemands(String spaceId);

    List<DemandDTO> getIterationNotHandleDemands(String iterationId);
}
