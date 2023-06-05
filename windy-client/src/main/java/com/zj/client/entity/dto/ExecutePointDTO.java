package com.zj.client.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.feature.executor.compare.CompareDefine;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.client.feature.executor.vo.VariableDefine;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutePointDTO {

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

    public static ExecutePoint toExecutePoint(ExecutePointDTO dto) {
        ExecutePoint point = new ExecutePoint();
        point.setFeatureId(dto.getFeatureId());
        point.setPointId(dto.getPointId());
        point.setDescription(dto.getDescription());
        point.setCompareDefine(JSON.toJSONString(dto.getCompareDefine()));
        point.setVariables(JSON.toJSONString(dto.getVariableDefine()));
        point.setFeatureInfo(JSON.toJSONString(dto.getExecutorUnit()));
        point.setSortOrder(dto.getSortOrder());
        point.setTestStage(dto.getTestStage());
        point.setExecuteType(dto.getExecuteType());
        return point;
    }

    public static ExecutePointDTO toExecutePointDTO(ExecutePoint executePoint){
        ExecutePointDTO dto = new ExecutePointDTO();
        dto.setDescription(executePoint.getDescription());
        dto.setPointId(executePoint.getPointId());
        dto.setExecuteType(executePoint.getExecuteType());
        dto.setFeatureId(executePoint.getFeatureId());
        dto.setExecutorUnit(JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class));
        dto.setCompareDefine(JSON.parseArray(executePoint.getCompareDefine(),CompareDefine.class));
        dto.setVariableDefine(JSON.parseArray(executePoint.getVariables(),VariableDefine.class));
        dto.setTestStage(executePoint.getTestStage());
        dto.setSortOrder(executePoint.getSortOrder());
        return dto;
    }

}
