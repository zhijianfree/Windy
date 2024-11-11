package com.zj.domain.repository.auth;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.auth.UserBO;

import java.util.List;

public interface IUserRepository {
    UserBO getUserByUserName(String userName);

    UserBO getUserByUserId(String userId);
    PageSize<UserBO> getGroupUserPage(String groupId, Integer page, Integer size);
    List<UserBO> getGroupUserList(String groupId);

    Boolean createUser(UserBO userBO);

    Boolean updateUser(UserBO userBO);

    Boolean deleteUser(String userId);

    List<UserBO> getUserByUserList(List<String> userIds);

    List<UserBO> fuzzyQueryUserName(String name);
}
