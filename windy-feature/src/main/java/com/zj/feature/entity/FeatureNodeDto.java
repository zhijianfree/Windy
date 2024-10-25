package com.zj.feature.entity;

import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/30
 */
@Data
public class FeatureNodeDto {

  /**
   * 用例Id
   */
  private String featureId;

  /**
   * 用例名称
   */
  @NotNull
  private String featureName;

  /**
   * 用例类型
   */
  private Integer featureType;

  /**
   * 测试集Id
   */
  private String testCaseId;

  /**
   * 排序
   */
  private Integer sortOrder;

  /**
   * 用例的测试步骤
   */
  private String testStep;

  /**
   * 父节点Id
   */
  private String parentId;

  private Long createTime;

  private Long updateTime;

  /**
   * 用例状态
   */
  private Integer status;

  /**
   * 子节点列表
   */
  private List<FeatureNodeDto> children;

  public static FeatureNodeDto toNode(FeatureInfoDto featureInfo) {
    FeatureNodeDto featureNodeDto = OrikaUtil.convert(featureInfo, FeatureNodeDto.class);
    featureNodeDto.setChildren(new ArrayList<>());
    return featureNodeDto;
  }
}
