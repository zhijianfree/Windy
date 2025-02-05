package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class ExecuteTemplate {

    private Long id;
    /**
     * 模版Id
     */
    private String templateId;

    /**
     * 模版类型 {@link com.zj.common.enums.TemplateType}
     */
    private Integer templateType;

    /**
     * 模版执行方式 {@link com.zj.common.enums.InvokerType}
     */
    private Integer invokeType;

    /**
     * 如果invokeType是本地调用service就是全路径的类名
     * 如果invokeType是HTTP调用service就是请求的url
     */
    private String service;

    /**
     * 如果invokeType是本地调用method就是类的方法名
     * 如果invokeType是HTTP调用method就是HTTP请求的METHOD(POST/GET/PUT/DELETE)
     */
    private String method;

    /**
     * 如果invokeType是HTTP时有值，代表HTTP的Header
     */
    private String header;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 模版描述
     */
    private String description;

    /**
     * 模版请求参数
     */
    private String param;

    /**
     * 模版创建者
     */
    private String owner;

    /**
     * 模版来源，如果是插件模版就此字段代表关联的插件ID
     */
    private String source;

    /**
     * 关联的模版Id
     */
    private String relatedId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
