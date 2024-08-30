package com.zj.domain.repository.auth;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.auth.RoleDto;

import java.util.List;

public interface IRoleRepository {
    PageSize<RoleDto> getRolePage(Integer page, Integer size);

    Boolean createRole(RoleDto roleDto);

    Boolean updateRole(RoleDto roleDto);

    Boolean deleteRole(String roleId);

    RoleDto getRole(String roleId);

    PageSize<RoleDto> getGroupRolePage(String groupId, Integer page, Integer size);

    List<RoleDto> getAllRoles();

    Boolean bindRole(String relationId, List<String> roleIds);

    boolean unbind(String relationId);

    boolean isRoleBind(String roleId);
}
