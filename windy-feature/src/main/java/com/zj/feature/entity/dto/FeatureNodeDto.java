package com.zj.feature.entity.dto;

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
  private String featureId;
  @NotNull
  private String featureName;
  @NotNull
  private String author;
  private String modify;
  private Integer featureType;
  private List<String> tags;
  private String testStep;
  private String testCaseId;
  private Long createTime;
  private Long updateTime;
  private List<ExecutePointDto> testFeatures;
  private List<FeatureNodeDto> children;

  public static FeatureNodeDto toNode(FeatureInfoDto featureInfo) {
    FeatureNodeDto featureNodeDto = OrikaUtil.convert(featureInfo, FeatureNodeDto.class);
    featureNodeDto.setChildren(new ArrayList<>());
    return featureNodeDto;
  }
}
