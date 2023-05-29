package com.zj.feature.entity.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeatureInfoVo {
    private String featureId;
    @NotNull
    private String featureName;
    private String author;
    private String parentId;
    private String modify;
    @NotNull
    private Integer featureType;
    private List<String> tags;
    private String testStep;
    @NotEmpty
    private String testCaseId;
    private Long createTime;
    private Long updateTime;
    private List<ExecutePointVo> testFeatures;

}
