package com.zj.feature.executor.vo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author falcon
 * @since 2023/1/5
 */
public class ExecuteContext {

  private final ConcurrentHashMap<String, Object> contextMap = new ConcurrentHashMap<>();

  public void set(String key, Object value) {
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
}
