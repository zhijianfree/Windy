package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberDto;
import com.zj.domain.entity.po.service.ResourceMember;

import java.util.List;

public interface IMemberRepository {

    List<ResourceMember> getResourceMembersByUser(String userId);

    List<UserBO> queryResourceMembers(String resourceId);

    boolean addResourceMember(ResourceMemberDto resourceMemberDto);

    boolean deleteResourceMember(String resourceId, String userId);
}
