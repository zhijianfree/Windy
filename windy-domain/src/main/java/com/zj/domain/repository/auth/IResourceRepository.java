package com.zj.domain.repository.auth;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.auth.ResourceBO;

import java.util.List;

public interface IResourceRepository {
    List<ResourceBO> getResourceByUserId(String userId);
    List<ResourceBO> getMenuByUserId(String userId);

    PageSize<ResourceBO> getResources(Integer page, Integer size);

    List<ResourceBO> getAllResources();

    Boolean createResource(ResourceBO resourceBO);

    Boolean updateResource(ResourceBO resourceBO);

    Boolean deleteResource(String resourceId);

    ResourceBO getResource(String resourceId);

    boolean resourceBind(String relationId, List<String> resourceIds);

    List<ResourceBO> getRoleResources(String roleId);

    boolean isResourceBind(String resourceId);

    boolean unbindResource(String roleId);
}
