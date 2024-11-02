package com.zj.auth.service;

import com.zj.auth.entity.ResourceBind;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.auth.ResourceBO;
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
    private final IAuthService authService;

    public ResourceService(IResourceRepository resourceRepository, UniqueIdService uniqueIdService,
                           PermissionService permissionService, IAuthService authService) {
        this.resourceRepository = resourceRepository;
        this.uniqueIdService = uniqueIdService;
        this.permissionService = permissionService;
        this.authService = authService;
    }

    public PageSize<ResourceBO> getResources(Integer page, Integer size) {
        return resourceRepository.getResources(page, size);
    }

    public List<ResourceBO> getAllResources() {
        return resourceRepository.getAllResources();
    }

    public Boolean createResource(ResourceBO resourceBO) {
        resourceBO.setResourceId(uniqueIdService.getUniqueId());
        return resourceRepository.createResource(resourceBO);
    }

    public Boolean updateResource(String resourceId, ResourceBO resourceBO) {
        resourceBO.setResourceId(resourceId);
        return resourceRepository.updateResource(resourceBO);
    }

    public Boolean deleteResource(String resourceId) {
        boolean resourceBind = resourceRepository.isResourceBind(resourceId);
        if (resourceBind) {
            log.info("resource is bind , can not delete ={}", resourceId);
            throw new ApiException(ErrorCode.RESOURCE_IS_BIND);
        }
        return resourceRepository.deleteResource(resourceId);
    }

    public ResourceBO getResource(String resourceId) {
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

    public List<ResourceBO> getRoleResources(String roleId) {
        return resourceRepository.getRoleResources(roleId);
    }

    public List<ResourceBO> getUserMenuList() {
        return resourceRepository.getMenuByUserId(authService.getCurrentUserId());
    }
}
