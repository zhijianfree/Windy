package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.IterationBO;

import java.util.List;

public interface IterationRepository {

    List<IterationBO> getIterationList(String spaceId, List<String> iterationIds);
    List<IterationBO> getSpaceNotHandleIterations(String spaceId);

    IterationBO createIteration(IterationBO iterationBO);

    IterationBO getIteration(String iterationId);

    boolean deleteIteration(String iterationId);

    boolean updateIteration(IterationBO iterationBO);
}
