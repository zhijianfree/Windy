package com.zj.auth.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupUserTree {

    private String userId;

    private String name;

    private String parentId;

    private Boolean isGroup;

    private List<GroupUserTree> children = new ArrayList<>();
}
