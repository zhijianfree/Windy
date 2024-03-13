package com.zj.feature.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.common.feature.ExecutorUnit;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.feature.entity.vo.CompareDefine;
import com.zj.feature.entity.vo.VariableDefine;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutePointVo {

  private String pointId;

  private String featureId;

  private Integer executeType;

  private String description;

  private String templateId;

  @NotNull
  private ExecutorUnit executorUnit;

  private List<CompareDefine> compareDefine;

  private List<VariableDefine> variableDefine;

  @NotNull
  @Max(3)
  @Min(1)
  private Integer testStage;
  private Integer sortOrder;

  public static ExecutePointDto toExecutePoint(ExecutePointVo dto) {
    ExecutePointDto point = new ExecutePointDto();
    point.setFeatureId(dto.getFeatureId());
    point.setPointId(dto.getPointId());
    point.setDescription(dto.getDescription());
    point.setSortOrder(dto.getSortOrder());
    point.setTemplateId(dto.getTemplateId());
    point.setTestStage(dto.getTestStage());
    point.setExecuteType(dto.getExecuteType());
    point.setCompareDefine(JSON.toJSONString(dto.getCompareDefine()));
    point.setVariables(JSON.toJSONString(dto.getVariableDefine()));
    point.setFeatureInfo(JSON.toJSONString(dto.getExecutorUnit()));
    return point;
  }

  public static ExecutePointVo toExecutePointDTO(ExecutePointDto executePoint) {
    ExecutePointVo dto = new ExecutePointVo();
    dto.setDescription(executePoint.getDescription());
    dto.setPointId(executePoint.getPointId());
    dto.setExecuteType(executePoint.getExecuteType());
    dto.setFeatureId(executePoint.getFeatureId());
    dto.setTestStage(executePoint.getTestStage());
    dto.setSortOrder(executePoint.getSortOrder());
    dto.setTemplateId(executePoint.getTemplateId());
    dto.setExecutorUnit(JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class));
    dto.setCompareDefine(JSON.parseArray(executePoint.getCompareDefine(), CompareDefine.class));
    dto.setVariableDefine(JSON.parseArray(executePoint.getVariables(), VariableDefine.class));
    return dto;
  }

}
