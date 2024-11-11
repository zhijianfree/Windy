package com.zj.domain.repository.auth;

import com.zj.domain.entity.bo.auth.GroupBO;

import java.util.List;

public interface IGroupRepository {
    boolean createGroup(GroupBO groupBO);

    List<GroupBO> getGroups();

    boolean updateGroup(GroupBO groupBO);

    boolean deleteGroup(String groupId);

    GroupBO getGroup(String groupId);
}
