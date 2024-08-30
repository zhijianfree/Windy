package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class ServiceMember {

    private Long id;

    private String serviceId;

    private String userId;

    private Long createTime;
}
