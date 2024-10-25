package com.zj.feature.entity;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class TagFilterDto {

  /**
   * 过滤的标签列表
   * */
  @NotEmpty
  private List<String> tags;

  /**
   * 测试集Id
   * */
  @NotBlank
  private String testCaseId;
}
