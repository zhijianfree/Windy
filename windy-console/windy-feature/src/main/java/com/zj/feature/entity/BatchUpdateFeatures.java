package com.zj.feature.entity;

import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateFeatures {

    private List<FeatureOrder> featureOrders;
}
