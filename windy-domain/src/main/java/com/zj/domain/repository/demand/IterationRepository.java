package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.demand.IterationDTO;

import java.util.List;

public interface IterationRepository {

    List<IterationDTO> getIterationList(String spaceId, List<String> iterationIds);
    List<IterationDTO> getSpaceNotHandleIterations(String spaceId);

    IterationDTO createIteration(IterationDTO iterationDTO);

    IterationDTO getIteration(String iterationId);

    boolean deleteIteration(String iterationId);

    boolean updateIteration(IterationDTO iterationDTO);
}
