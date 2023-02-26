package com.zj.feature.entity.dto;

import lombok.Data;

@Data
public class MicroserviceDTO {

    /**
     * 服务Id
     * */
    private String serviceId;

    /**
     * 服务名
     * */
    private String serviceName;

    /**
     * 服务描述
     * */
    private String description;

    /**
     * 服务拥有者
     * */
    private String owner;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;

}
