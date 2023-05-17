package com.zj.domain.entity.dto.feature;

import lombok.Data;

@Data
public class FeatureInfoDto {
    private String testCaseId;
    private String featureId;
    private String featureName;
    private String author;
    private String modify;
    private String testStep;
    private String parentId;
    private Integer featureType;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
