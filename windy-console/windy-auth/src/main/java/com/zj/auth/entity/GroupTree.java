package com.zj.auth.entity;

import lombok.Data;

import java.util.List;

@Data
public class GroupTree {

    private String groupId;

    private String groupName;

    private String parentId;
    /**
     * 组织描述
     */
    private String description;

    private List<GroupTree> children;
}
