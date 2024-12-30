package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.ResourceBO;
import com.zj.domain.entity.enums.ResourceType;
import com.zj.domain.entity.po.auth.Resource;
import com.zj.domain.entity.po.auth.RoleResource;
import com.zj.domain.entity.po.auth.User;
import com.zj.domain.entity.po.auth.UserRole;
import com.zj.domain.mapper.auth.ResourceMapper;
import com.zj.domain.mapper.auth.RoleResourceMapper;
import com.zj.domain.mapper.auth.UserMapper;
import com.zj.domain.mapper.auth.UserRoleMapper;
import com.zj.domain.repository.auth.IResourceRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ResourceRepositoryImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceRepository {

    private final UserRoleMapper userRoleMapper;

    private final UserMapper userMapper;
    private final RoleResourceMapper roleResourceMapper;

    public ResourceRepositoryImpl(UserRoleMapper userRoleMapper, UserMapper userMapper, RoleResourceMapper roleResourceMapper) {
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.roleResourceMapper = roleResourceMapper;
    }

    @Override
    public List<ResourceBO> getResourceByUserId(String userId) {
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

    @Override
    public List<ResourceBO> getMenuByUserId(String userId) {
        List<ResourceBO> resourceList = getResourceByUserId(userId);
        User user = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserId, userId));
        List<ResourceBO> groupResourceList = getResourceByUserId(user.getGroupId());
        List<ResourceBO> allResources = ListUtils.union(resourceList, groupResourceList);
        return allResources.stream().filter(resource -> Objects.equals(resource.getResourceType(),
                ResourceType.MENU.getType())).collect(Collectors.toList());
    }

    private List<ResourceBO> getResourceList(List<RoleResource> roleResources) {
        if (CollectionUtils.isEmpty(roleResources)) {
            return Collections.emptyList();
        }
        List<String> resourceIds = roleResources.stream().map(RoleResource::getResourceId).collect(Collectors.toList());
        List<Resource> resources = list(Wrappers.lambdaQuery(Resource.class).in(Resource::getResourceId, resourceIds));
        return OrikaUtil.convertList(resources, ResourceBO.class);
    }

    @Override
    public PageSize<ResourceBO> getResources(Integer page, Integer size) {
        LambdaQueryWrapper<Resource> wrapper = Wrappers.lambdaQuery(Resource.class).orderByDesc(Resource::getCreateTime);
        IPage<Resource> pageQuery = new Page<>(page, size);
        IPage<Resource> rolePage = page(pageQuery, wrapper);
        PageSize<ResourceBO> pageSize = new PageSize<>();
        pageSize.setTotal(rolePage.getTotal());
        if (CollectionUtils.isNotEmpty(rolePage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(rolePage.getRecords(), ResourceBO.class));
        }
        return pageSize;
    }

    @Override
    public List<ResourceBO> getAllResources() {
        return OrikaUtil.convertList(list(), ResourceBO.class);
    }

    @Override
    public boolean createResource(ResourceBO resourceBO) {
        Resource resource = OrikaUtil.convert(resourceBO, Resource.class);
        resource.setCreateTime(System.currentTimeMillis());
        resource.setUpdateTime(System.currentTimeMillis());
        return save(resource);
    }

    @Override
    public boolean updateResource(ResourceBO resourceBO) {
        Resource resource = OrikaUtil.convert(resourceBO, Resource.class);
        resource.setUpdateTime(System.currentTimeMillis());
        return update(resource, Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId,
                resource.getResourceId()));
    }

    @Override
    public boolean deleteResource(String resourceId) {
        return remove(Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId, resourceId));
    }

    @Override
    public ResourceBO getResource(String resourceId) {
        Resource resource = getOne(Wrappers.lambdaUpdate(Resource.class).eq(Resource::getResourceId, resourceId));
        return OrikaUtil.convert(resource, ResourceBO.class);
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
    public List<ResourceBO> getRoleResources(String roleId) {
        List<RoleResource> roleResources =
                roleResourceMapper.selectList(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getRoleId,
                        roleId));
        return getResourceList(roleResources);
    }

    @Override
    public boolean isResourceBind(String resourceId) {
        List<RoleResource> roleResources =
                roleResourceMapper.selectList(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getResourceId
                        , resourceId));
        return CollectionUtils.isNotEmpty(roleResources);
    }

    @Override
    public boolean unbindResource(String roleId) {
        return roleResourceMapper.delete(Wrappers.lambdaQuery(RoleResource.class).eq(RoleResource::getRoleId, roleId)) > 0;
    }
}
