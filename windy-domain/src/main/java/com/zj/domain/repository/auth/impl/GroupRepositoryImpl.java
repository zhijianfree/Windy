package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.auth.GroupDto;
import com.zj.domain.entity.po.auth.Group;
import com.zj.domain.mapper.auth.GroupMapper;
import com.zj.domain.repository.auth.IGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupRepositoryImpl extends ServiceImpl<GroupMapper, Group> implements IGroupRepository {

    @Override
    public boolean createGroup(GroupDto groupDto) {
        Group group = OrikaUtil.convert(groupDto, Group.class);
        group.setCreateTime(System.currentTimeMillis());
        group.setUpdateTime(System.currentTimeMillis());
        return save(group);
    }

    @Override
    public List<GroupDto> getGroups() {
        return OrikaUtil.convertList(list(), GroupDto.class);
    }

    @Override
    public boolean updateGroup(GroupDto groupDto) {
        Group group = OrikaUtil.convert(groupDto, Group.class);
        group.setUpdateTime(System.currentTimeMillis());
        return update(group, Wrappers.lambdaUpdate(Group.class).eq(Group::getGroupId, group.getGroupId()));
    }

    @Override
    public boolean deleteGroup(String groupId) {
        return remove(Wrappers.lambdaUpdate(Group.class).eq(Group::getGroupId, groupId));
    }

    @Override
    public GroupDto getGroup(String groupId) {
        Group group = getOne(Wrappers.lambdaUpdate(Group.class).eq(Group::getGroupId, groupId));
        return OrikaUtil.convert(group, GroupDto.class);
    }


}
