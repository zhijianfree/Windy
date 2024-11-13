package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.mapper.service.ResourceMemberMapper;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IMemberRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberRepositoryImpl extends ServiceImpl<ResourceMemberMapper, ResourceMember> implements IMemberRepository {

    private final IUserRepository userRepository;

    public MemberRepositoryImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserBO> queryResourceMembers(String resourceId) {
        List<ResourceMember> members = list(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getResourceId, resourceId));
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<String> userIds = members.stream().map(ResourceMember::getUserId).collect(Collectors.toList());
        return userRepository.getUserByUserList(userIds);
    }

    @Override
    public boolean addResourceMember(ResourceMemberBO resourceMemberBO) {
        ResourceMember resourceMember = OrikaUtil.convert(resourceMemberBO, ResourceMember.class);
        resourceMember.setCreateTime(System.currentTimeMillis());
        return save(resourceMember);
    }

    @Override
    public List<ResourceMember> getResourceMembersByUser(String userId) {
       return list(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getUserId,
                userId));
    }

    @Override
    public boolean deleteResourceMember(String resourceId, String userId) {
        return remove(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getResourceId,
                resourceId).eq(ResourceMember::getUserId, userId));
    }
}
