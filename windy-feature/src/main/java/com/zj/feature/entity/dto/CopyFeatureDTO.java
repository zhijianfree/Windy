package com.zj.feature.entity.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class CopyFeatureDTO {
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
