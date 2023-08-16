package com.zj.feature.entity.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Data
public class BatchTemplates {

  @NotBlank
  private String pluginId;

  @NotEmpty
  private List<ExecuteTemplateVo> templates;

}
