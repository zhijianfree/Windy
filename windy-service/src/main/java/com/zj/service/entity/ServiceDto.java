package com.zj.service.entity;

import com.zj.common.model.K8SContainerParams;
import lombok.Data;

@Data
public class ServiceDto {

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
     * 服务拥有者
     * */
    private String owner;

    /**
     * 服务优先级，用来服务列表排序
     * */
    private Integer priority;

    /**
     * 服务部署容器配置
     */
    private K8SContainerParams containerParams;

}
