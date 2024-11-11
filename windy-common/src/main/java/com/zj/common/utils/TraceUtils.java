package com.zj.common.utils;

import org.slf4j.MDC;

public class TraceUtils {
    private static final String MDC_TID_KEY = "tid";

    public static void putTrace(String traceId) {
        MDC.put(MDC_TID_KEY, traceId);
    }

    public static void removeTrace() {
        MDC.remove(MDC_TID_KEY);
    }

    public static String getTraceId() {
        return MDC.get(MDC_TID_KEY);
    }
}
