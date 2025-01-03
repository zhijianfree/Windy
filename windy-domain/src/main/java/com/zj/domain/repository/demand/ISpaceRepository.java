package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.SpaceBO;

import java.util.List;

public interface ISpaceRepository {

    /**
     * 获取空间列表
     * @return 空间列表
     */
    List<SpaceBO> getSpaceList();

    /**
     * 创建空间
     * @param spaceBO 空间信息
     * @return 是否成功
     */
    SpaceBO createSpace(SpaceBO spaceBO);

    /**
     * 获取空间详情
     * @param spaceId 空间ID
     * @return 空间信息
     */
    SpaceBO getSpace(String spaceId);

    /**
     * 更新空间
     * @param spaceBO 空间信息
     * @return 是否成功
     */
    boolean updateSpace(SpaceBO spaceBO);

    /**
     * 删除空间
     * @param spaceId 空间ID
     * @return 是否成功
     */
    boolean deleteSpace(String spaceId);
}
