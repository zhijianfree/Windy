package com.zj.client.handler.feature.executor.vo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guyuelan
 * @since 2023/1/5
 */
public class ExecuteContext {

  private final Map<String, Object> contextMap = new ConcurrentHashMap<>();

  public void set(String key, Object value) {
    if (Objects.isNull(key) || Objects.isNull(value)) {
      return;
    }
    contextMap.put(key, value);
  }

  public Object get(String key) {
    return contextMap.get(key);
  }

  public void remove(String key) {
    contextMap.remove(key);
  }

  public Map<String, Object> toMap() {
    return contextMap;
  }

  public void bindMap(Map<String, Object> mapContext) {
    contextMap.putAll(mapContext);
  }
}
