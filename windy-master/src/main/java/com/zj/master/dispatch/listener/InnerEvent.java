package com.zj.master.dispatch.listener;

import com.zj.common.enums.LogType;
import com.zj.master.entity.enums.EventType;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/18
 */
@Data
public class InnerEvent {

  private EventType eventType;

  private String targetId;

  private LogType logType;
}
