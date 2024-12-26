package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.BugQueryBO;

import java.util.List;

public interface IBugRepository {
    /**
     * 获取用户的bug列表
     * @param bugQueryBO 查询条件
     * @return bug列表
     */
    PageSize<BugBO> getUserBugs(BugQueryBO bugQueryBO);

    /**
     * 获取用户负责处理的bug列表
     * @param bugQueryBO 查询条件
     * @return bug列表
     */
    PageSize<BugBO> getUserRelatedBugs(BugQueryBO bugQueryBO);

    /**
     * 创建bug
     * @param bugBO bug信息
     * @return 是否成功
     */
    boolean createBug(BugBO bugBO);

    /**
     * 更新bug
     * @param bugBO bug信息
     * @return 是否成功
     */
    boolean updateBug(BugBO bugBO);

    /**
     * 获取bug详情
     * @param bugId 缺陷ID
     * @return bug信息
     */
    BugBO getBug(String bugId);

    /**
     * 删除bug
     * @param bugId 缺陷ID
     * @return 是否成功
     */
    boolean deleteBug(String bugId);

    /**
     * 获取迭代的bug列表
     * @param iterationId 迭代ID
     * @return bug列表
     */
    List<BugBO> getIterationBugs(String iterationId);

    /**
     * 模糊查询缺陷列表
     * @param queryName 缺陷名称
     * @return bug列表
     */
    List<BugBO> getBugsFuzzyByName(String queryName);

    /**
     * 获取空间未处理的bug列表
     * @param spaceId 空间ID
     * @return bug列表
     */
    List<BugBO> getSpaceNotHandleBugs(String spaceId);

    /**
     * 获取迭代未处理的bug列表
     * @param iterationId 迭代ID
     * @return bug列表
     */
    List<BugBO> getIterationNotHandleBugs(String iterationId);

    /**
     * 获取未完成的bug列表
     * @param bugIds 缺陷ID列表
     * @return bug列表
     */
    List<BugBO> getNotCompleteBugs(List<String> bugIds);

    /**
     * 批量更新缺陷状态
     * @param bugIds 缺陷ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<String> bugIds, int status);
}
