package com.zj.domain.entity.dto.auth;

import lombok.Data;

@Data
public class RoleResourceDto {
    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 角色Id
     */
    private String roleId;
}
