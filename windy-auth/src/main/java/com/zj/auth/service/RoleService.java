package com.zj.auth.service;

import com.zj.auth.entity.RoleBind;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.auth.RoleBO;
import com.zj.domain.repository.auth.IRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RoleService {

    private final IRoleRepository roleRepository;
    private final UniqueIdService uniqueIdService;
    private final PermissionService permissionService;

    public RoleService(IRoleRepository roleRepository, UniqueIdService uniqueIdService, PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.uniqueIdService = uniqueIdService;
        this.permissionService = permissionService;
    }

    public PageSize<RoleBO> getRoles(Integer page, Integer size) {
        return roleRepository.getRolePage(page, size);
    }

    public Boolean createRole(RoleBO roleBO) {
        roleBO.setRoleId(uniqueIdService.getUniqueId());
        return roleRepository.createRole(roleBO);
    }

    public Boolean updateRole(String roleId, RoleBO roleBO) {
        roleBO.setRoleId(roleId);
        return roleRepository.updateRole(roleBO);
    }

    public Boolean deleteRole(String roleId) {
        boolean roleBind = roleRepository.isRoleBind(roleId);
        if (roleBind) {
            log.info("role is bind , can not delete ={}", roleId);
            throw new ApiException(ErrorCode.ROLE_IS_BIND);
        }
        return roleRepository.deleteRole(roleId);
    }

    public RoleBO getRole(String roleId) {
        return roleRepository.getRole(roleId);
    }

    public PageSize<RoleBO> getGroupRoles(String groupId, Integer page, Integer size) {
        return roleRepository.getGroupRolePage(groupId, page, size);
    }

    public List<RoleBO> getAllRoles() {
        return roleRepository.getAllRoles();
    }

    @Transactional
    public Boolean bindRole(RoleBind roleBind) {
        boolean unbindResult = roleRepository.unbind(roleBind.getRelationId());
        log.info("un bind roles result = {}", unbindResult);
        boolean result = roleRepository.bindRole(roleBind.getRelationId(), roleBind.getRoleIds());
        if (result) {
            permissionService.removeUserCache(roleBind.getRelationId());
        }
        return result;
    }
}
