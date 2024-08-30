package com.zj.domain.repository.auth;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.auth.ResourceDto;

import java.util.List;

public interface IResourceRepository {
    List<ResourceDto> getResourceByyUserId(String userId);

    PageSize<ResourceDto> getResources(Integer page, Integer size);

    List<ResourceDto> getAllResources();

    Boolean createResource(ResourceDto resourceDto);

    Boolean updateResource(ResourceDto resourceDto);

    Boolean deleteResource(String resourceId);

    ResourceDto getResource(String resourceId);

    Boolean resourceBind(String relationId, List<String> resourceIds);

    List<ResourceDto> getRoleResources(String roleId);

    boolean isResourceBind(String resourceId);
}
