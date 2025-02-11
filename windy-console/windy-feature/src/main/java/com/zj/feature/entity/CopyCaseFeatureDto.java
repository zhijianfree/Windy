package com.zj.feature.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class CopyCaseFeatureDto {
  /**
   * 用例Id列表
   * */
  @NotEmpty
  private List<String> featureIds;

  /**
   * 测试集Id
   * */
  @NotBlank
  private String testCaseId;
}
