package com.zj.domain.repository.auth;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.auth.UserDto;

import java.util.List;

public interface IUserRepository {
    UserDto getUserByUserName(String userName);

    UserDto getUserByUserId(String userId);
    PageSize<UserDto> getGroupUserPage(String groupId, Integer page, Integer size);

    Boolean createUser(UserDto userDto);

    Boolean updateUser(UserDto userDto);

    Boolean deleteUser(String userId);

    List<UserDto> getUserByUserList(List<String> userIds);

    List<UserDto> fuzzyQueryUserName(String name);
}
