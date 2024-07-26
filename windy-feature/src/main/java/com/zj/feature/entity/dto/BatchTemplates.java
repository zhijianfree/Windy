package com.zj.feature.entity.dto;

import com.zj.common.feature.ExecuteTemplateVo;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Data
public class BatchTemplates {

  private String pluginId;

  @NotEmpty
  private List<ExecuteTemplateVo> templates;

}
