package com.zj.domain.entity.bo.auth;

import lombok.Data;

@Data
public class GroupBO {

    /**
     * 组织ID
     */
    private String groupId;

    /**
     * 组织名称
     */
    private String groupName;

    private String parentId;

    /**
     * 组织描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
