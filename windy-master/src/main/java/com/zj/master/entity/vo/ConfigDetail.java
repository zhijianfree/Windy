package com.zj.master.entity.vo;

import com.zj.domain.entity.bo.pipeline.CompareResult;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/6/20
 */
@Data
public class ConfigDetail {
  /**
   * 执行的动作Id
   * */
  private String actionId;

  private Map<String, String> paramList;

  /**
   * node执行的参数
   * */
  private List<CompareResult> compareInfo;
}
