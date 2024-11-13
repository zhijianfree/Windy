package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.BuildToolBO;
import com.zj.domain.entity.po.service.BuildTool;
import com.zj.domain.mapper.service.IBuildToolMapper;
import com.zj.domain.repository.service.IBuildToolRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuildToolRepository extends ServiceImpl<IBuildToolMapper, BuildTool> implements IBuildToolRepository {
    @Override
    public boolean saveBuildTool(BuildToolBO buildToolBO) {
        BuildTool buildTool = OrikaUtil.convert(buildToolBO, BuildTool.class);
        buildTool.setCreateTime(System.currentTimeMillis());
        buildTool.setUpdateTime(System.currentTimeMillis());
        return save(buildTool);
    }

    @Override
    public boolean deleteBuildTool(String buildToolId) {
        return remove(Wrappers.lambdaQuery(BuildTool.class).eq(BuildTool::getToolId, buildToolId));
    }

    @Override
    public List<BuildToolBO> getBuildToolList() {
        List<BuildTool> buildTools = list();
        return OrikaUtil.convertList(buildTools, BuildToolBO.class);
    }
}
