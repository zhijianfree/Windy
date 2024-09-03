package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.service.ResourceMemberDto;
import com.zj.domain.entity.po.service.ResourceMember;

import java.util.List;

public interface IMemberRepository {

    List<ResourceMember> getResourceMembersByUser(String userId);

    List<UserDto> queryResourceMembers(String resourceId);

    boolean addResourceMember(ResourceMemberDto resourceMemberDto);

    boolean deleteResourceMember(String resourceId, String userId);
}
