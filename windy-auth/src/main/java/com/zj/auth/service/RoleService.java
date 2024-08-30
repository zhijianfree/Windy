package com.zj.auth.service;

import com.zj.auth.entity.RoleBind;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.RoleDto;
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

    public PageSize<RoleDto> getRoles(Integer page, Integer size) {
        return roleRepository.getRolePage(page, size);
    }

    public Boolean createRole(RoleDto roleDto) {
        roleDto.setRoleId(uniqueIdService.getUniqueId());
        return roleRepository.createRole(roleDto);
    }

    public Boolean updateRole(String roleId, RoleDto roleDto) {
        roleDto.setRoleId(roleId);
        return roleRepository.updateRole(roleDto);
    }

    public Boolean deleteRole(String roleId) {
        boolean roleBind = roleRepository.isRoleBind(roleId);
        if (roleBind) {
            log.info("role is bind , can not delete ={}", roleId);
            throw new ApiException(ErrorCode.ROLE_IS_BIND);
        }
        return roleRepository.deleteRole(roleId);
    }

    public RoleDto getRole(String roleId) {
        return roleRepository.getRole(roleId);
    }

    public PageSize<RoleDto> getGroupRoles(String groupId, Integer page, Integer size) {
        return roleRepository.getGroupRolePage(groupId, page, size);
    }

    public List<RoleDto> getAllRoles() {
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
