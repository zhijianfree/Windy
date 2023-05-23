package com.zj.common.model;

import com.zj.common.enums.LogType;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
public class StopDispatch {
  private String targetId;
  private LogType logType;
}
