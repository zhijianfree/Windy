package com.zj.common.adapter.trace;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author guyuelan
 * @since 2023/6/26
 */
@Component
@WebFilter(urlPatterns = "/**", filterName = "requestTraceFilter")
public class TidInterceptor implements Filter {

  public static final String MDC_TID_KEY = "tid";
  public static final String HTTP_HEADER_TRACE_ID = "REQUEST-TRACE-ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String tid = httpRequest.getHeader(HTTP_HEADER_TRACE_ID);
      if (StringUtils.isBlank(tid)) {
        tid = UUID.randomUUID().toString().replace("-","");
      }
      MDC.put(MDC_TID_KEY, tid);
      chain.doFilter(request, response);
    } finally {
      // 清除MDC的traceId值，确保在请求结束后不会影响其他请求的日志
      MDC.remove(MDC_TID_KEY);
    }
  }
}
