package com.zj.common.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Data
public class PluginInfo {

  private String pluginName;

  private byte[] fileData;
}
