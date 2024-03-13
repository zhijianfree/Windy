package com.zj.service.entity;

import lombok.Data;

import java.util.List;

@Data
public class GenerateTemplate {

    /**
     * api列表
     */
    private List<String> apiIds;

    /**
     * 模版调用方式
     */
    private Integer invokeType;

    /**
     * 服务Id
     */
    private String serviceId;

    /**
     * 关联的模版Id
     */
    private String relatedId;

}
