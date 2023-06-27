package com.zj.common.monitor.pool;

import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;

/**
 * @author falcon
 * @since 2023/6/27
 */
public class WrapperRunnable {
public static <V> Callable<V> wrapperCall(Callable<V> callable){
  Map<String, String> contextMap = MDC.getCopyOfContextMap();
  return () -> {
    try {
      if (contextMap != null) {
        MDC.setContextMap(contextMap);
      }
      return callable.call();
    }catch (Exception ignore){}finally {
      MDC.clear();
    }
    return null;
  };
}

  public static Runnable wrapper(Runnable runnable){
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    return () -> {
      try {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        }
        runnable.run();
      } finally {
        MDC.clear();
      }
    };
  }


}
