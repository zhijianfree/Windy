package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.SpaceBO;

import java.util.List;

public interface ISpaceRepository {

    List<SpaceBO> getSpaceList();

    SpaceBO createSpace(SpaceBO spaceBO);

    SpaceBO getSpace(String spaceId);

    boolean updateSpace(SpaceBO spaceBO);

    boolean deleteSpace(String spaceId);
}
