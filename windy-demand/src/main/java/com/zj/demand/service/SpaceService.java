package com.zj.demand.service;

import com.zj.common.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.SpaceDTO;
import com.zj.domain.repository.demand.ISpaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SpaceService {

    private final ISpaceRepository spaceRepository;
    private final UniqueIdService uniqueIdService;
    private final IAuthService authService;

    public SpaceService(ISpaceRepository spaceRepository, UniqueIdService uniqueIdService, IAuthService authService) {
        this.spaceRepository = spaceRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
    }

    public List<SpaceDTO> getSpaceList() {
        return spaceRepository.getSpaceList();
    }

    public SpaceDTO createSpace(SpaceDTO spaceDTO) {
        spaceDTO.setUserId(authService.getCurrentUserId());
        spaceDTO.setSpaceId(uniqueIdService.getUniqueId());
        return spaceRepository.createSpace(spaceDTO);
    }

    public boolean updateSpace(String spaceId, SpaceDTO spaceDTO) {
        SpaceDTO space = spaceRepository.getSpace(spaceId);
        if (Objects.isNull(space)) {
            log.info("space not exist = {}", spaceId);
            throw new ApiException(ErrorCode.SPACE_NOT_EXIST);
        }
        spaceDTO.setSpaceId(spaceId);
        return spaceRepository.updateSpace(spaceDTO);
    }

    public boolean deleteSpace(String spaceId) {
        return spaceRepository.deleteSpace(spaceId);
    }

    public SpaceDTO getSpace(String spaceId) {
        return spaceRepository.getSpace(spaceId);
    }
}
