package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.demand.SpaceDTO;

import java.util.List;

public interface ISpaceRepository {

    List<SpaceDTO> getSpaceList();

    SpaceDTO createSpace(SpaceDTO spaceDTO);

    SpaceDTO getSpace(String spaceId);

    boolean updateSpace(SpaceDTO spaceDTO);

    boolean deleteSpace(String spaceId);
}
