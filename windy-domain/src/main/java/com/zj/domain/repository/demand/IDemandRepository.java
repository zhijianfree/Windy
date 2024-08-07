package com.zj.domain.repository.demand;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.DemandDTO;
import com.zj.domain.entity.dto.demand.DemandQuery;

public interface IDemandRepository {
    boolean createDemand(DemandDTO demand);

    PageSize<DemandDTO> getDemandPage(DemandQuery demandQuery);

    boolean updateDemand(DemandDTO demand);

    DemandDTO getDemand(String demandId);

    boolean deleteDemand(String demandId);

    PageSize<DemandDTO> getRelatedDemands(String proposer, Integer page, Integer size);
}
