package com.zj.domain.entity.bo.service;

import lombok.Data;

@Data
public class ResourceMemberBO {

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 关联ID
     */
    private String relationId;

    /**
     * 关联成员类型
     */
    private String memberType;
}
