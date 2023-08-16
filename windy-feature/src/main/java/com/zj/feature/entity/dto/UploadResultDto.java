package com.zj.feature.entity.dto;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@Data
public class UploadResultDto {

  private String pluginId;

  private List<ExecuteTemplateVo> templateDefines;
}
