package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class Microservice {
    private Long id;

    /**
     * 服务Id
     * */
    private String serviceId;

    /**
     * 服务名
     * */
    private String serviceName;

    /**
     * 服务git地址
     * */
    private String gitUrl;

    /**
     * 服务拥有者
     * */
    private String owner;

    /**
     * 服务描述
     * */
    private String description;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;
}
