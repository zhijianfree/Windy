package com.zj.common.entity.feature;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExecutePointDto {

    private String pointId;

    private String featureId;

    private Integer executeType;

    private String description;

    @NotNull
    private ExecutorUnit executorUnit;

    private List<CompareDefine> compareDefine;

    private List<VariableDefine> variableDefine;

    @NotNull
    @Max(3)
    @Min(1)
    private Integer testStage;

    private Integer sortOrder;


}
