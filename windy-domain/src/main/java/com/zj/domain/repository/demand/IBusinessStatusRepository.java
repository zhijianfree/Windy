package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.BusinessStatusBO;

import java.util.List;

public interface IBusinessStatusRepository {
    List<BusinessStatusBO> getDemandStatuses();
    List<BusinessStatusBO> getIterationStatuses();
    List<BusinessStatusBO> getBugStatuses();
    List<BusinessStatusBO> getWorkTaskStatuses();
    List<BusinessStatusBO> getDemandTags();
    List<BusinessStatusBO> getBugTags();
}
