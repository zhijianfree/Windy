package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.po.service.ResourceMember;

import java.util.List;

public interface IMemberRepository {

    List<ResourceMember> getResourceMembersByUser(String userId);

    List<UserBO> queryResourceMembers(String resourceId);

    boolean addResourceMember(ResourceMemberBO resourceMemberBO);

    boolean deleteResourceMember(String resourceId, String userId);
}
