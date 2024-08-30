package com.zj.auth.service;

import com.zj.auth.entity.ResourceBind;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.ResourceDto;
import com.zj.domain.repository.auth.IResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ResourceService {

    private final IResourceRepository resourceRepository;
    private final UniqueIdService uniqueIdService;
    private final PermissionService permissionService;

    public ResourceService(IResourceRepository resourceRepository, UniqueIdService uniqueIdService, PermissionService permissionService) {
        this.resourceRepository = resourceRepository;
        this.uniqueIdService = uniqueIdService;
        this.permissionService = permissionService;
    }

    public PageSize<ResourceDto> getResources(Integer page, Integer size) {
        return resourceRepository.getResources(page, size);
    }

    public List<ResourceDto> getAllResources() {
        return resourceRepository.getAllResources();
    }

    public Boolean createResource(ResourceDto resourceDto) {
        resourceDto.setResourceId(uniqueIdService.getUniqueId());
        return resourceRepository.createResource(resourceDto);
    }

    public Boolean updateResource(String resourceId, ResourceDto resourceDto) {
        resourceDto.setResourceId(resourceId);
        return resourceRepository.updateResource(resourceDto);
    }

    public Boolean deleteResource(String resourceId) {
        boolean resourceBind = resourceRepository.isResourceBind(resourceId);
        if (resourceBind) {
            log.info("resource is bind , can not delete ={}", resourceId);
            throw new ApiException(ErrorCode.RESOURCE_IS_BIND);
        }
        return resourceRepository.deleteResource(resourceId);
    }

    public ResourceDto getResource(String resourceId) {
        return resourceRepository.getResource(resourceId);
    }

    public Boolean resourceBind(ResourceBind resourceBind) {
        boolean unbindResource = resourceRepository.unbindResource(resourceBind.getRelationId());
        log.info("unbind resource result={}", unbindResource);
        boolean result = resourceRepository.resourceBind(resourceBind.getRelationId(), resourceBind.getResourceIds());
        if (result) {
            permissionService.removeAllAuthCache();
        }
        return result;
    }

    public List<ResourceDto> getRoleResources(String roleId) {
        return resourceRepository.getRoleResources(roleId);
    }
}
