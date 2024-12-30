package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class ResourceMember {

    private Long id;

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

    private Long createTime;
}
