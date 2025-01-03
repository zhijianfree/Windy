package com.zj.domain.entity.po.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@Data
public class PluginInfo {

  private Long id;

  /**
   * 插件名称
   * */
  private String pluginName;

  /**
   * 插件Id
   * */
  private String pluginId;

  /**
   * 插件状态
   * */
  private Integer status;

  /**
   * 插件类型 默认1
   * */
  private Integer pluginType;

  /**
   * 插件文件流
   * */
  private byte[] fileData;

  /**
   * 文件hash计算值
   */
  private String hashValue;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 修改时间
   */
  private Long updateTime;
}
