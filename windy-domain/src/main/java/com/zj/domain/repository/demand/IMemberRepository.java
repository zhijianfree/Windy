package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;

import java.util.List;

public interface IMemberRepository {

    List<ResourceMemberBO> getByRelationMember(String relationId, String memberType);

    List<ResourceMemberBO> getResourceRelations(String resourceId, String memberType);

    List<UserBO> getResourceUserMembers(String resourceId, String memberType);

    boolean addResourceMember(ResourceMemberBO resourceMemberBO);

    boolean deleteResourceMember(String resourceId, String userId);

    Boolean batchUpdateMembers(List<ResourceMemberBO> resourceMembers, String type);
}
