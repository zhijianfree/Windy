package com.zj.feature.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.common.feature.CompareDefine;
import com.zj.common.feature.ExecutorUnit;
import com.zj.common.feature.VariableDefine;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutePointVo {

  /**
   * 执行点Id
   */
  private String pointId;

  /**
   * 用例Id
   */
  @NotBlank(message = "执行点用例Id不能为空")
  private String featureId;

  /**
   * 执行点执行类型
   */
  private Integer executeType;

  /**
   * 描述
   */
  private String description;

  /**
   * 执行点关联的模版Id
   */
  private String templateId;

  /**
   * 执行点关联模版执行的数据
   */
  @NotNull
  private @Valid ExecutorUnit executorUnit;

  private List<@Valid CompareDefine> compareDefine;

  private List<@Valid VariableDefine> variableDefine;

  /**
   * 执行点所属阶段
   */
  @Max(3)
  @Min(1)
  @NotNull
  private Integer testStage;

  /**
   * 执行点排序
   */
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
