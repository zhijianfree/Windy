package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.auth.ResourceDto;
import com.zj.domain.entity.po.auth.Resource;
import com.zj.domain.entity.po.auth.RoleResource;
import com.zj.domain.entity.po.auth.UserRole;
import com.zj.domain.mapper.auth.ResourceMapper;
import com.zj.domain.mapper.auth.RoleResourceMapper;
import com.zj.domain.mapper.auth.UserRoleMapper;
import com.zj.domain.repository.auth.IResourceRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ResourceRepositoryImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceRepository {

    private final UserRoleMapper userRoleMapper;
    private final RoleResourceMapper roleResourceMapper;

    public ResourceRepositoryImpl(UserRoleMapper userRoleMapper, RoleResourceMapper roleResourceMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleResourceMapper = roleResourceMapper;
    }

    @Override
    public List<ResourceDto> getResourceByyUserId(String userId) {
        List<UserRole> userRoles =
                userRoleMapper.selectList(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        List<String> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<RoleResource> roleResources =
                roleResourceMapper.selectList(Wrappers.lambdaQuery(RoleResource.class).in(RoleResource::getRoleId,
                        roleIds));
        return getResourceList(roleResources);
    }

    private List<ResourceDto> getResourceList(List<RoleResource> roleResources) {
        if (CollectionUtils.isEmpty(roleResources)) {
            return Collections.emptyList();
        }
        List<String> resourceIds = roleResources.stream().map(RoleResource::getResourceId).collect(Collectors.toList());
        List<Resource> resources = list(Wrappers.lambdaQuery(Resource.class).in(Resource::getResourceId, resourceIds));
        return OrikaUtil.convertList(resources, ResourceDto.class);
    }

    @Override
    public PageSize<ResourceDto> getResources(Integer page, Integer size) {
        LambdaQueryWrapper<Resource> wrapper = Wrappers.lambdaQuery(Resource.class);
        IPage<Resource> pageQuery = new Page<>(page, size);
        IPage<Resource> rolePage = page(pageQuery, wrapper);
        PageSize<ResourceDto> pageSize = new PageSize<>();
        pageSize.setTotal(rolePage.getTotal());
        if (CollectionUtils.isNotEmpty(rolePage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(rolePage.getRecords(), ResourceDto.class));
        }
        return pageSize;
    }

    @Override
    public List<ResourceDto> getAllResources() {
        return OrikaUtil.convertList(list(), ResourceDto.class);
    }

    @Override
    public Boolean createResource(ResourceDto resourceDto) {
        Resource resource = OrikaUtil.convert(resourceDto, Resource.class);
        resource.setCreateTime(System.currentTimeMillis());
        resource.setUpdateTime(System.currentTimeMillis());
        return save(resource);
    }

    @Override
    public Boolean updateResource(ResourceDto resourceDto) {
        Resource resource = OrikaUtil.convert(resourceDto, Resource.class);
        resource.setUpdateTime(System.currentTimeMillis());
        return update(resource, Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId,
                resource.getResourceId()));
    }

    @Override
    public Boolean deleteResource(String resourceId) {
        return remove(Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId, resourceId));
    }

    @Override
    public ResourceDto getResource(String resourceId) {
        Resource resource = getOne(Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId, resourceId));
        return OrikaUtil.convert(resource, ResourceDto.class);
    }

    @Override
    @Transactional
    public boolean resourceBind(String relationId, List<String> resourceIds) {
        resourceIds.forEach(resourceId -> {
            RoleResource userRole = new RoleResource();
            userRole.setRoleId(relationId);
            userRole.setResourceId(resourceId);
            roleResourceMapper.insert(userRole);
        });
        return true;
    }

    @Override
    public List<ResourceDto> getRoleResources(String roleId) {
        List<RoleResource> roleResources = roleResourceMapper.selectList(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getRoleId, roleId));
        return getResourceList(roleResources);
    }

    @Override
    public boolean isResourceBind(String resourceId) {
        List<RoleResource> roleResources =
                roleResourceMapper.selectList(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getResourceId, resourceId));
        return CollectionUtils.isNotEmpty(roleResources);
    }

    @Override
    public boolean unbindResource(String roleId) {
        return roleResourceMapper.delete(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getRoleId, roleId)) > 0;
    }
}
