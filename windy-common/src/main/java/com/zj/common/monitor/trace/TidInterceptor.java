package com.zj.common.monitor.trace;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author falcon
 * @since 2023/6/26
 */
public class TidInterceptor implements HandlerInterceptor {

  public static final String MDC_TID_KEY = "tid";
  public static final String HTTP_HEADER_TRACE_ID = "REQUEST-TRACE-ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String tid = request.getHeader(HTTP_HEADER_TRACE_ID);
    if (StringUtils.isBlank(tid)) {
      tid = UUID.randomUUID().toString().replace("-","");
    }
    MDC.put(MDC_TID_KEY, tid);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    // 在请求处理完毕后，移除tid参数
    MDC.remove(MDC_TID_KEY);
  }
}
