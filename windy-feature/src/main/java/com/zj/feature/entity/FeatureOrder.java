package com.zj.feature.entity;

import lombok.Data;

@Data
public class FeatureOrder {

    private String featureId;

    private Integer sortOrder;

    private String parentId;
}
