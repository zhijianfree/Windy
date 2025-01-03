package com.zj.domain.repository.auth;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.auth.ResourceBO;

import java.util.List;

public interface IResourceRepository {
    /**
     * 根据用户ID获取资源
     * @param userId 用户ID
     * @return 资源列表
     */
    List<ResourceBO> getResourceByUserId(String userId);

    /**
     * 根据用户ID获取菜单
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<ResourceBO> getMenuByUserId(String userId);

    /**
     * 获取资源列表
     * @param page 页码
     * @param size 每页数量
     * @return 资源列表
     */
    PageSize<ResourceBO> getResources(Integer page, Integer size);

    /**
     * 获取所有资源
     * @return 资源列表
     */
    List<ResourceBO> getAllResources();

    /**
     * 创建资源
     * @param resourceBO 资源信息
     * @return 是否成功
     */
    boolean createResource(ResourceBO resourceBO);

    /**
     * 更新资源
     * @param resourceBO 资源信息
     * @return 是否成功
     */
    boolean updateResource(ResourceBO resourceBO);

    /**
     * 根据ID删除资源
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean deleteResource(String resourceId);

    /**
     * 根据ID获取资源
     * @param resourceId 资源ID
     * @return 资源信息
     */
    ResourceBO getResource(String resourceId);

    /**
     * 绑定资源
     * @param relationId 关联ID
     * @param resourceIds 资源ID列表
     * @return 是否成功
     */
    boolean resourceBind(String relationId, List<String> resourceIds);

    /**
     * 根据角色ID获取资源
     * @param roleId 角色ID
     * @return 资源列表
     */
    List<ResourceBO> getRoleResources(String roleId);

    /**
     * 判断资源是否已绑定
     * @param resourceId 资源ID
     * @return 是否已绑定
     */
    boolean isResourceBind(String resourceId);

    /**
     * 解绑资源
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean unbindResource(String roleId);
}
