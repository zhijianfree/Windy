package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.BuildToolBO;

import java.util.List;

public interface IBuildToolRepository {

    /**
     * 保存构建工具
     * @param buildTool 构建工具信息
     * @return 是否成功
     */
    boolean saveBuildTool(BuildToolBO buildTool);

    /**
     * 删除构建工具
     * @param buildToolId 构建工具ID
     * @return 是否成功
     */
    boolean deleteBuildTool(String buildToolId);

    /**
     * 获取构建工具列表
     * @return 构建工具列表
     */
    List<BuildToolBO> getBuildToolList();

    /**
     * 更新构建工具
     * @param buildToolBO 构建工具信息
     * @return 是否成功
     */
    boolean updateBuildTool(BuildToolBO buildToolBO);
}
