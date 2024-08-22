package com.zj.domain.entity.dto.auth;

import lombok.Data;

@Data
public class UserGroupDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 组织ID
     */
    private String groupId;
}
