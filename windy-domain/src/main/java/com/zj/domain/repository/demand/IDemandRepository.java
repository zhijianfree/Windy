package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.DemandQueryBO;

import java.util.List;

public interface IDemandRepository {
    /**
     * 创建需求
     * @param demand 需求信息
     * @return 是否成功
     */
    boolean createDemand(DemandBO demand);

    /**
     * 分页获取需求列表
     * @param demandQueryBO 查询条件
     * @return 需求列表
     */
    PageSize<DemandBO> getDemandPage(DemandQueryBO demandQueryBO);

    /**
     * 更新需求
     * @param demand 需求信息
     * @return 是否成功
     */
    boolean updateDemand(DemandBO demand);

    /**
     * 获取需求详情
     * @param demandId 需求ID
     * @return 需求信息
     */
    DemandBO getDemand(String demandId);

    /**
     * 删除需求
     * @param demandId 需求ID
     * @return 是否成功
     */
    boolean deleteDemand(String demandId);

    /**
     * 获取迭代需求
     * @param iterationId 迭代ID
     * @return 需求列表
     */
    List<DemandBO> getIterationDemand(String iterationId);

    /**
     * 模糊查询需求列表
     * @param queryName 查询名称
     * @return 需求列表
     */
    List<DemandBO> getDemandsByFuzzName(String queryName);

    /**
     * 获取空间未处理需求
     * @param spaceId 空间ID
     * @return 需求列表
     */
    List<DemandBO> getSpaceNotHandleDemands(String spaceId);

    /**
     * 获取迭代未处理需求
     * @param iterationId 迭代ID
     * @return 需求列表
     */
    List<DemandBO> getIterationNotHandleDemands(String iterationId);

    /**
     * 获取未完成需求
     * @param demandIds 需求ID列表
     * @return 需求列表
     */
    List<DemandBO> getNotCompleteDemandByIds(List<String> demandIds);

    /**
     * 批量更新需求状态
     * @param demandIds 需求ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<String> demandIds, int status);

    /**
     * 批量处理更新需求进入处理中
     * @param demandIds 需求列表
     * @return 是否成功
     */
    boolean batchUpdateProcessing(List<String> demandIds);
}
