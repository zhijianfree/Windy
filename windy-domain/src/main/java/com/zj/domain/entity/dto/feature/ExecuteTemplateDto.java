package com.zj.domain.entity.dto.feature;

import lombok.Data;

@Data
public class ExecuteTemplateDto {
    private String templateId;
    private Integer templateType;
    private Integer invokeType;
    private String method;
    private String name;
    private String description;
    private String service;
    private String header;
    private String param;
    private String author;
    private Long createTime;
    private Long updateTime;
}
