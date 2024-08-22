package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.auth.Group;
import com.zj.domain.mapper.auth.GroupMapper;
import com.zj.domain.repository.auth.IGroupRepository;
import org.springframework.stereotype.Repository;

@Repository
public class GroupRepositoryImpl extends ServiceImpl<GroupMapper, Group> implements IGroupRepository {
}
