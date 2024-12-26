package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.IterationBO;

import java.util.List;

public interface IterationRepository {

    /**
     * 获取空间迭代列表
     * @param spaceId 空间ID
     * @return 迭代列表
     */
    List<IterationBO> getIterationList(String spaceId, List<String> iterationIds);

    /**
     * 获取空间未处理迭代
     * @param spaceId 空间ID
     * @return 迭代列表
     */
    List<IterationBO> getSpaceNotHandleIterations(String spaceId);

    /**
     * 创建迭代
     * @param iterationBO 迭代信息
     * @return 是否成功
     */
    IterationBO createIteration(IterationBO iterationBO);

    /**
     * 获取迭代详情
     * @param iterationId 迭代ID
     * @return 迭代信息
     */
    IterationBO getIteration(String iterationId);

    /**
     * 删除迭代
     * @param iterationId 迭代ID
     * @return 是否成功
     */
    boolean deleteIteration(String iterationId);

    /**
     * 更新迭代
     * @param iterationBO 迭代信息
     * @return 是否成功
     */
    boolean updateIteration(IterationBO iterationBO);
}
