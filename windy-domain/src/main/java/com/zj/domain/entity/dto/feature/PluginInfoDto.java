package com.zj.domain.entity.dto.feature;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/7/14
 */
@Data
public class PluginInfoDto {

  private String pluginName;

  private String pluginId;

  private String templateId;

  private Integer pluginType;

  private byte[] fileData;

  private Long createTime;

  private Long updateTime;
}
