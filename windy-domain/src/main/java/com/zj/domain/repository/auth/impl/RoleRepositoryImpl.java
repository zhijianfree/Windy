package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.auth.Role;
import com.zj.domain.mapper.auth.RoleMapper;
import com.zj.domain.repository.auth.IRoleRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryImpl extends ServiceImpl<RoleMapper, Role> implements IRoleRepository {
}
