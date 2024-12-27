package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.enums.MemberType;

import java.util.List;

public interface IMemberRepository {

    /**
     * 根据关联ID和成员类型获取成员列表
     * @param relationId 关联ID
     * @param memberType 成员类型
     * @return  成员列表
     */
    List<ResourceMemberBO> getByRelationMember(String relationId, String memberType);

    /**
     * 根据资源ID和成员类型获取成员列表
     * @param resourceId 资源ID
     * @param memberType 成员类型
     * @return 成员列表
     */
    List<ResourceMemberBO> getResourceRelations(String resourceId, String memberType);

    /**
     * 根据资源ID和成员类型获取用户列表
     * @param resourceId 资源ID
     * @param memberType 成员类型
     * @return 成员列表
     */
    List<UserBO> getResourceUserMembers(String resourceId, String memberType);

    /**
     * 添加资源成员
     * @param resourceMemberBO 资源成员信息
     * @return 是否成功
     */
    boolean addResourceMember(ResourceMemberBO resourceMemberBO);

    /**
     * 删除资源成员
     * @param resourceId 资源ID
     * @param relationId 用户ID
     * @return 是否成功
     */
    boolean deleteResourceMember(String resourceId, String relationId);

    /**
     * 删除资源成员
     * @param resourceId 资源ID
     * @param memberType 成员类型
     * @return 是否成功
     */
    boolean deleteResourceMemberByType(String resourceId, MemberType memberType);

    /**
     * 批量更新成员
     * @param resourceMembers 资源成员列表
     * @param type 类型
     * @return 是否成功
     */
    Boolean batchUpdateMembers(List<ResourceMemberBO> resourceMembers, String type);
}
