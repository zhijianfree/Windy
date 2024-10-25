package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.demand.BusinessStatusDto;

import java.util.List;

public interface IBusinessStatusRepository {
    List<BusinessStatusDto> getDemandStatuses();
    List<BusinessStatusDto> getIterationStatuses();
    List<BusinessStatusDto> getBugStatuses();
    List<BusinessStatusDto> getWorkTaskStatuses();
    List<BusinessStatusDto> getDemandTags();
    List<BusinessStatusDto> getBugTags();
}
