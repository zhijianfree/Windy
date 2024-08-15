package com.zj.domain.repository.demand;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.BugDTO;
import com.zj.domain.entity.dto.demand.BugQuery;

import java.util.List;

public interface IBugRepository {
    PageSize<BugDTO> getUserBugs(BugQuery bugQuery);
    PageSize<BugDTO> getUserRelatedBugs(BugQuery bugQuery);

    boolean createBug(BugDTO bugDTO);

    boolean updateBug(BugDTO bugDTO);

    BugDTO getBug(String bugId);

    boolean deleteBug(String bugId);

    Integer countIteration(String iterationId);

    List<BugDTO> getIterationBugs(String iterationId);

    List<BugDTO> getBugsByName(String queryName);
}
