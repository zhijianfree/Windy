package com.zj.feature.entity.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeatureInfoDTO {
    private String featureId;
    @NotNull
    private String featureName;
    private String author;
    private String parentId;
    private String modify;
    private Integer featureType;
    private List<String> tags;
    private String testStep;
    private String testCaseId;
    private Long createTime;
    private Long updateTime;
    private List<ExecutePointDTO> testFeatures;

}
