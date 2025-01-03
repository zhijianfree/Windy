package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.RoleBO;
import com.zj.domain.entity.po.auth.Role;
import com.zj.domain.entity.po.auth.UserRole;
import com.zj.domain.mapper.auth.RoleMapper;
import com.zj.domain.mapper.auth.UserRoleMapper;
import com.zj.domain.repository.auth.IRoleRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryImpl extends ServiceImpl<RoleMapper, Role> implements IRoleRepository {
    private final UserRoleMapper userRoleMapper;

    public RoleRepositoryImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public PageSize<RoleBO> getRolePage(Integer page, Integer size) {
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery(Role.class).orderByDesc(Role::getCreateTime);
        IPage<Role> pageQuery = new Page<>(page, size);
        return exchangePageSize(pageQuery, wrapper);
    }

    private PageSize<RoleBO> exchangePageSize(IPage<Role> pageQuery, LambdaQueryWrapper<Role> wrapper) {
        IPage<Role> bugPage = page(pageQuery, wrapper);
        PageSize<RoleBO> pageSize = new PageSize<>();
        pageSize.setTotal(bugPage.getTotal());
        if (CollectionUtils.isNotEmpty(bugPage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(bugPage.getRecords(), RoleBO.class));
        }
        return pageSize;
    }

    @Override
    public Boolean createRole(RoleBO roleBO) {
        Role role = OrikaUtil.convert(roleBO, Role.class);
        role.setCreateTime(System.currentTimeMillis());
        role.setUpdateTime(System.currentTimeMillis());
        return save(role);
    }

    @Override
    public Boolean updateRole(RoleBO roleBO) {
        Role role = OrikaUtil.convert(roleBO, Role.class);
        role.setUpdateTime(System.currentTimeMillis());
        return update(role, Wrappers.lambdaUpdate(Role.class).eq(Role::getRoleId, role.getRoleId()));
    }

    @Override
    public Boolean deleteRole(String roleId) {
        return remove(Wrappers.lambdaQuery(Role.class).eq(Role::getRoleId, roleId));
    }

    @Override
    public RoleBO getRole(String roleId) {
        Role role = getOne(Wrappers.lambdaQuery(Role.class).eq(Role::getRoleId, roleId));
        return OrikaUtil.convert(role, RoleBO.class);
    }

    @Override
    public PageSize<RoleBO> getGroupRolePage(String groupId, Integer page, Integer size) {
        List<UserRole> userRoles =
                userRoleMapper.selectList(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, groupId));
        if (CollectionUtils.isEmpty(userRoles)) {
            return new PageSize<>();
        }
        List<String> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery(Role.class).in(Role::getRoleId, roleIds).orderByDesc(Role::getCreateTime);
        IPage<Role> pageQuery = new Page<>(page, size);

        IPage<Role> rolePage = page(pageQuery, wrapper);
        PageSize<RoleBO> pageSize = new PageSize<>();
        pageSize.setTotal(rolePage.getTotal());
        if (CollectionUtils.isNotEmpty(rolePage.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(rolePage.getRecords(), RoleBO.class));
        }
        return pageSize;
    }

    @Override
    public List<RoleBO> getAllRoles() {
        return OrikaUtil.convertList(list(), RoleBO.class);
    }

    @Override
    @Transactional
    public boolean bindRole(String relationId, List<String> roleIds) {
        roleIds.forEach(roleId ->{
            UserRole userRole = new UserRole();
            userRole.setRoleId(roleId);
            userRole.setUserId(relationId);
            userRoleMapper.insert(userRole);
        });
        return true;
    }

    @Override
    public boolean unbind(String relationId) {
        return userRoleMapper.delete(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, relationId)) >= 1;
    }

    @Override
    public boolean isRoleBind(String roleId) {
        List<UserRole> userRoles = userRoleMapper.selectList(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getRoleId, roleId));
        return CollectionUtils.isNotEmpty(userRoles);
    }
}
