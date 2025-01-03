package com.zj.master.dispatch.listener;

import com.zj.common.enums.LogType;
import com.zj.master.entity.enums.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
@NoArgsConstructor
public class InternalEvent {

  private EventType eventType;

  private String targetId;

  private LogType logType;

  private Map<String,Object> context;

  public InternalEvent(EventType eventType, String targetId, Map<String, Object> context) {
    this.eventType = eventType;
    this.targetId = targetId;
    this.context = context;
  }
}
