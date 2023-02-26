package com.zj.feature.entity.po;

import lombok.Data;

@Data
public class FeatureInfo {
    private Long id;
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
