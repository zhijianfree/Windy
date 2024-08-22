package com.zj.domain.repository.auth.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.auth.User;
import com.zj.domain.mapper.auth.UserMapper;
import com.zj.domain.repository.auth.IUserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements IUserRepository {
}
