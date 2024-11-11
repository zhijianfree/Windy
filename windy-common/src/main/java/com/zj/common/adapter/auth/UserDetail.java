package com.zj.common.adapter.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetail {

    /**
     * 用户Id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户所属组织
     */
    private String groupId;
}
