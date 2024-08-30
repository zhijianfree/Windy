package com.zj.domain.entity.dto.auth;

import lombok.Data;

@Data
public class UserRoleDto {

    /**
     * 用户ID
     * */
    private String userId;

    /**
     * 角色ID
     * */
    private String roleId;
}
