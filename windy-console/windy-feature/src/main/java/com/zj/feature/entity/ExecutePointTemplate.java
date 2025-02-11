package com.zj.feature.entity;

import lombok.Data;

@Data
public class ExecutePointTemplate {

    /**
     * 模版所属服务
     */
    private String service;

    /**
     * 请求信息
     */
    private String request;

    /**
     * 模版执行方式
     */
    private Integer invokeType;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 模版描述
     */
    private String description;
}
