package com.zj.domain.repository.auth;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.auth.RoleBO;

import java.util.List;

public interface IRoleRepository {
    /**
     * 分页获取角色列表
     * @param page 页码
     * @param size 每页数量
     * @return 角色列表
     */
    PageSize<RoleBO> getRolePage(Integer page, Integer size);

    /**
     * 创建角色
     * @param roleBO 角色信息
     * @return 是否成功
     */
    Boolean createRole(RoleBO roleBO);

    /**
     * 更新角色
     * @param roleBO 角色信息
     * @return 是否成功
     */
    Boolean updateRole(RoleBO roleBO);

    /**
     * 删除角色
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteRole(String roleId);

    /**
     * 根据ID获取角色
     * @param roleId 角色ID
     * @return 角色信息
     */
    RoleBO getRole(String roleId);

    /**
     * 分页获取组织角色列表
     * @param groupId 组织ID
     * @return 角色列表
     */
    PageSize<RoleBO> getGroupRolePage(String groupId, Integer page, Integer size);

    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<RoleBO> getAllRoles();

    /**
     * 绑定角色
     * @param relationId 关联ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean bindRole(String relationId, List<String> roleIds);

    /**
     * 解绑角色
     * @param relationId 关联ID
     * @return 是否成功
     */
    boolean unbind(String relationId);

    /**
     * 判断角色是否已绑定
     * @param roleId 角色ID
     * @return 是否已绑定
     */
    boolean isRoleBind(String roleId);
}
