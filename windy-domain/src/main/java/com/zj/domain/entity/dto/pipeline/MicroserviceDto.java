package com.zj.domain.entity.dto.pipeline;

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
