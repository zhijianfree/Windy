package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.SpaceBO;
import com.zj.domain.entity.po.demand.Space;
import com.zj.domain.mapper.demand.SpaceMapper;
import com.zj.domain.repository.demand.ISpaceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements ISpaceRepository {

    @Override
    public List<SpaceBO> getSpaceList() {
        List<Space> list = list();
        return OrikaUtil.convertList(list, SpaceBO.class);
    }

    @Override
    public SpaceBO createSpace(SpaceBO spaceBO) {
        spaceBO.setCreateTime(System.currentTimeMillis());
        spaceBO.setUpdateTime(System.currentTimeMillis());
        Space space = OrikaUtil.convert(spaceBO, Space.class);
        return save(space) ? spaceBO : null;
    }

    @Override
    public SpaceBO getSpace(String spaceId) {
        Space space = getOne(Wrappers.lambdaQuery(Space.class).eq(Space::getSpaceId, spaceId));
        return OrikaUtil.convert(space, SpaceBO.class);
    }

    @Override
    public boolean updateSpace(SpaceBO spaceBO) {
        Space space = OrikaUtil.convert(spaceBO, Space.class);
        space.setUpdateTime(System.currentTimeMillis());
        return update(space, Wrappers.lambdaUpdate(Space.class).eq(Space::getSpaceId, space.getSpaceId()));
    }

    @Override
    public boolean deleteSpace(String spaceId) {
        return remove(Wrappers.lambdaUpdate(Space.class).eq(Space::getSpaceId, spaceId));
    }
}
