package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class ExecuteTemplate {
    private Long id;
    private String templateId;
    private Integer templateType;
    private Integer invokeType;
    private String method;
    private String name;
    private String description;
    private String service;
    private String param;
    private String header;
    private String author;
    private Long createTime;
    private Long updateTime;
}
