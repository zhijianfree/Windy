package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.demand.BusinessStatusDTO;

import java.util.List;

public interface BusinessStatusRepository {
    List<BusinessStatusDTO> getDemandStatuses();
}
