package com.zj.feature.entity;

import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.feature.VariableDefine;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutePointDto {

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

  public static ExecutePointDto toExecutePointDTO(ExecutePointBO executePoint) {
    ExecutePointDto executePointDto = new ExecutePointDto();
    executePointDto.setDescription(executePoint.getDescription());
    executePointDto.setPointId(executePoint.getPointId());
    executePointDto.setExecuteType(executePoint.getExecuteType());
    executePointDto.setFeatureId(executePoint.getFeatureId());
    executePointDto.setTestStage(executePoint.getTestStage());
    executePointDto.setSortOrder(executePoint.getSortOrder());
    executePointDto.setTemplateId(executePoint.getTemplateId());
    executePointDto.setExecutorUnit(executePoint.getExecutorUnit());
    executePointDto.setCompareDefine(executePoint.getCompareDefines());
    executePointDto.setVariableDefine(executePoint.getVariableDefines());
    return executePointDto;
  }

}
