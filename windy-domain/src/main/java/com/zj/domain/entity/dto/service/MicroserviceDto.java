package com.zj.domain.entity.dto.service;

import lombok.Data;

@Data
public class MicroserviceDto {

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
     * 服务描述
     * */
    private String description;

    /**
     * 服务优先级，用来服务列表排序
     * */
    private Integer priority;

    /**
     * 服务配置
     */
    private String serviceConfig;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;

}
