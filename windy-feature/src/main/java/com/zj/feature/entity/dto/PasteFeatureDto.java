package com.zj.feature.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class PasteFeatureDto {

  /**
   * 源用例
   * */
  @NotEmpty
  private List<String> featureIds;

  /**
   * 目标用例
   * */
  @NotBlank
  private String targetFeature;
}
