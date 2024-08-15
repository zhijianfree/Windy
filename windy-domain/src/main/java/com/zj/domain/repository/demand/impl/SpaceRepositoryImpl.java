package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.SpaceDTO;
import com.zj.domain.entity.po.demand.Space;
import com.zj.domain.mapper.demand.SpaceMapper;
import com.zj.domain.repository.demand.ISpaceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements ISpaceRepository {

    @Override
    public List<SpaceDTO> getSpaceList() {
        List<Space> list = list();
        return OrikaUtil.convertList(list, SpaceDTO.class);
    }

    @Override
    public SpaceDTO createSpace(SpaceDTO spaceDTO) {
        spaceDTO.setCreateTime(System.currentTimeMillis());
        spaceDTO.setUpdateTime(System.currentTimeMillis());
        Space space = OrikaUtil.convert(spaceDTO, Space.class);
        return save(space) ? spaceDTO : null;
    }

    @Override
    public SpaceDTO getSpace(String spaceId) {
        Space space = getOne(Wrappers.lambdaQuery(Space.class).eq(Space::getSpaceId, spaceId));
        return OrikaUtil.convert(space, SpaceDTO.class);
    }

    @Override
    public boolean updateSpace(SpaceDTO spaceDTO) {
        Space space = OrikaUtil.convert(spaceDTO, Space.class);
        space.setUpdateTime(System.currentTimeMillis());
        return update(space, Wrappers.lambdaUpdate(Space.class).eq(Space::getSpaceId, space.getSpaceId()));
    }

    @Override
    public boolean deleteSpace(String spaceId) {
        return remove(Wrappers.lambdaUpdate(Space.class).eq(Space::getSpaceId, spaceId));
    }
}
