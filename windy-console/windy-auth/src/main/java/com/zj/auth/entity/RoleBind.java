package com.zj.auth.entity;

import lombok.Data;

import java.util.List;

@Data
public class RoleBind {

    private String relationId;

    private List<String> roleIds;
}
