package com.zj.feature.entity.dto;

import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.po.feature.FeatureInfo;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/30
 */
@Data
public class FeatureNodeDTO {
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
  private List<ExecutePointDTO> testFeatures;
  private List<FeatureNodeDTO> children;

  public static FeatureNodeDTO toNode(FeatureInfoDto featureInfo) {
    FeatureNodeDTO featureNodeDTO = OrikaUtil.convert(featureInfo, FeatureNodeDTO.class);
    featureNodeDTO.setChildren(new ArrayList<>());
    return featureNodeDTO;
  }
}
