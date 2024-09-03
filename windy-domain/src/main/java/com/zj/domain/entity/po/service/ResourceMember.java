package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class ResourceMember {

    private Long id;

    private String resourceId;

    private String userId;

    private Long createTime;
}
