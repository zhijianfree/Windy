package com.zj.pipeline.entity.dto;

import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/20
 */
@Data
public class ConfigDetail {
  /**
   * node对应功能的服务地址
   * */
  private String url;

  /**
   * node执行的参数
   * */
  private String data;
}
