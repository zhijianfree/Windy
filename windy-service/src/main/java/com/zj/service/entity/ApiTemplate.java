package com.zj.service.entity;

import com.zj.plugin.loader.ParameterDefine;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApiTemplate {

    /**
     * http 请求的地址
     */
    private String service;

    /**
     * Http请求Header
     */
    private Map<String, String> headers;

    /**
     * HTTP请求方法
     */
    private String method;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 模版类型
     */
    private Integer templateType;

    /**
     * 模版调用方式
     */
    private Integer invokeType;

    /**
     * 模版描述
     */
    private String description;

    private List<ParameterDefine> params;
}
