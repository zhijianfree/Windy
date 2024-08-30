package com.zj.domain.repository.auth;

import com.zj.domain.entity.dto.auth.GroupDto;

import java.util.List;

public interface IGroupRepository {
    boolean createGroup(GroupDto groupDto);

    List<GroupDto> getGroups();

    boolean updateGroup(GroupDto groupDto);

    boolean deleteGroup(String groupId);

    GroupDto getGroup(String groupId);
}
