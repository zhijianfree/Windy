package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.enums.MemberType;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.mapper.service.ResourceMemberMapper;
import com.zj.domain.repository.auth.IUserRepository;
import com.zj.domain.repository.demand.IMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MemberRepositoryImpl extends ServiceImpl<ResourceMemberMapper, ResourceMember> implements IMemberRepository {

    private final IUserRepository userRepository;

    public MemberRepositoryImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserBO> getResourceUserMembers(String resourceId, String memberType) {
        List<ResourceMember> members = list(Wrappers.lambdaQuery(ResourceMember.class)
                .eq(ResourceMember::getResourceId, resourceId).eq(ResourceMember::getMemberType, memberType));
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<String> userIds = members.stream().map(ResourceMember::getRelationId).collect(Collectors.toList());
        return userRepository.getUserByUserList(userIds);
    }

    @Override
    public List<ResourceMemberBO> getResourceRelations(String resourceId, String memberType) {
        List<ResourceMember> members = list(Wrappers.lambdaQuery(ResourceMember.class)
                .eq(ResourceMember::getResourceId, resourceId).eq(ResourceMember::getMemberType, memberType));
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(members, ResourceMemberBO.class);
    }

    @Override
    public boolean addResourceMember(ResourceMemberBO resourceMemberBO) {
        ResourceMember resourceMember = OrikaUtil.convert(resourceMemberBO, ResourceMember.class);
        resourceMember.setCreateTime(System.currentTimeMillis());
        return save(resourceMember);
    }

    @Override
    public List<ResourceMemberBO> getByRelationMember(String relationId, String memberType) {
        List<ResourceMember> resourceMembers = list(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getRelationId,
                relationId).eq(ResourceMember::getMemberType, memberType));
        return OrikaUtil.convertList(resourceMembers, ResourceMemberBO.class);
    }

    @Override
    public boolean deleteResourceMember(String resourceId, String relationId) {
        return remove(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getResourceId,
                resourceId).eq(ResourceMember::getRelationId, relationId));
    }

    @Override
    @Transactional
    public Boolean batchUpdateMembers(List<ResourceMemberBO> resourceMembers, String memberType) {
        if (CollectionUtils.isEmpty(resourceMembers)) {
            return false;
        }
        ResourceMemberBO resourceMemberBO = resourceMembers.get(0);
        boolean removeResult = remove(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getResourceId,
                resourceMemberBO.getResourceId()).eq(ResourceMember::getMemberType, memberType));
        log.info("remove resource members result={}", removeResult);
        List<ResourceMember> resourceMemberList = OrikaUtil.convertList(resourceMembers, ResourceMember.class);
        return saveBatch(resourceMemberList);
    }

    @Override
    public boolean deleteResourceMemberByType(String resourceId, MemberType memberType) {
        return remove(Wrappers.lambdaQuery(ResourceMember.class).eq(ResourceMember::getResourceId,
                resourceId).eq(ResourceMember::getMemberType, memberType.getType()));
    }
}
