package com.zj.domain.repository.auth;

import com.zj.domain.entity.bo.auth.GroupBO;

import java.util.List;

public interface IGroupRepository {

    /**
     * 创建组织
     */
    boolean createGroup(GroupBO groupBO);

    /**
     * 获取组织列表
     */
    List<GroupBO> getGroups();

    /**
     * 更新组织
     */
    boolean updateGroup(GroupBO groupBO);

    /**
     * 删除组织
     */
    boolean deleteGroup(String groupId);

    /**
     * 根据ID获取组织
     */
    GroupBO getGroup(String groupId);
}
