package com.zj.domain.repository.auth;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.auth.RoleBO;

import java.util.List;

public interface IRoleRepository {
    PageSize<RoleBO> getRolePage(Integer page, Integer size);

    Boolean createRole(RoleBO roleBO);

    Boolean updateRole(RoleBO roleBO);

    Boolean deleteRole(String roleId);

    RoleBO getRole(String roleId);

    PageSize<RoleBO> getGroupRolePage(String groupId, Integer page, Integer size);

    List<RoleBO> getAllRoles();

    boolean bindRole(String relationId, List<String> roleIds);

    boolean unbind(String relationId);

    boolean isRoleBind(String roleId);
}
