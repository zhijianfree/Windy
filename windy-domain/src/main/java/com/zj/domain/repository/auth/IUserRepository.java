package com.zj.domain.repository.auth;

import com.zj.domain.entity.dto.auth.UserDto;

public interface IUserRepository {
    UserDto getUserByUserName(String userName);
}
