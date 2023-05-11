package com.zj.pipeline.entity.vo;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/20
 */
@Data
public class ConfigDetail {
  /**
   * 执行的动作Id
   * */
  private String actionId;

  private Map<String, String> requestContext;

  /**
   * node执行的参数
   * */
  private List<CompareResult> compareInfo;
}