package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;

import java.util.List;

public interface IBugRepository {
    PageSize<BugBO> getUserBugs(BugQueryBO bugQueryBO);
    PageSize<BugBO> getUserRelatedBugs(BugQueryBO bugQueryBO);

    boolean createBug(BugBO bugBO);

    boolean updateBug(BugBO bugBO);

    BugBO getBug(String bugId);

    boolean deleteBug(String bugId);

    Integer countIteration(String iterationId);

    List<BugBO> getIterationBugs(String iterationId);

    List<BugBO> getBugsByName(String queryName);

    List<BugBO> getSpaceNotHandleBugs(String spaceId);

    List<BugBO> getIterationNotHandleBugs(String iterationId);

    List<BugBO> getNotCompleteBugs(List<String> bugIds);

    boolean batchUpdateStatus(List<String> bugIds, int status);
}
