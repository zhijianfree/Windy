package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.BuildToolBO;

import java.util.List;

public interface IBuildToolRepository {

    boolean saveBuildTool(BuildToolBO buildTool);

    boolean deleteBuildTool(String buildToolId);

    List<BuildToolBO> getBuildToolList();

    boolean updateBuildTool(BuildToolBO buildToolBO);
}
