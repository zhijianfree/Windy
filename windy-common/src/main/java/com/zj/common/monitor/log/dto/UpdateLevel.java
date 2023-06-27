package com.zj.common.monitor.log.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2021/11/5
 */
@Data
public class UpdateLevel {

  private String logName;

  private String level;

  private Integer type = 3;
}
