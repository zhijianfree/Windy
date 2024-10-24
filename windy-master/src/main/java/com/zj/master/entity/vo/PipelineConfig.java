package com.zj.master.entity.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author falcon
 * @since 2023/7/19
 */
@Data
public class PipelineConfig {

  /**
   * 流水线定时点
   * */
  private String schedule;

  private Map<String, Object> paramList;
}
