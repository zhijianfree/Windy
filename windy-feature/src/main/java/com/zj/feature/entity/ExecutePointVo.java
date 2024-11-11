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

  public static ExecutePointVo toExecutePointDTO(ExecutePointBO executePoint) {
    ExecutePointVo executePointVo = new ExecutePointVo();
    executePointVo.setDescription(executePoint.getDescription());
    executePointVo.setPointId(executePoint.getPointId());
    executePointVo.setExecuteType(executePoint.getExecuteType());
    executePointVo.setFeatureId(executePoint.getFeatureId());
    executePointVo.setTestStage(executePoint.getTestStage());
    executePointVo.setSortOrder(executePoint.getSortOrder());
    executePointVo.setTemplateId(executePoint.getTemplateId());
    executePointVo.setExecutorUnit(executePoint.getExecutorUnit());
    executePointVo.setCompareDefine(executePoint.getCompareDefines());
    executePointVo.setVariableDefine(executePoint.getVariableDefines());
    return executePointVo;
  }

}
