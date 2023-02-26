package com.zj.feature.utils;

import java.util.Objects;

public class ExceptionUtils {
    private static final String LINE_SEPARATOR = "\r\n";

    public static String getSimplifyError(Throwable e){
        return getExceptionStackTace(e, 15);
    }

    private static String getExceptionStackTace(Throwable e, int line) {
        if (Objects.isNull(e)){
            return "";
        }

        String message = e.toString();
        StringBuilder stringBuilder = null;
        if (message.length() > 256){
            stringBuilder = new StringBuilder(message.substring(0, 255));
        }else {
            stringBuilder = new StringBuilder(message);
        }

        StackTraceElement[] elements = e.getStackTrace();
        for (int i = 0; i < elements.length; i ++){
            stringBuilder.append(LINE_SEPARATOR).append("\tat");
            if (elements[i].toString().length() > 256){
                stringBuilder.append(elements[i].toString(), 0, 255);
            }else{
                stringBuilder.append(elements[i].toString());
            }
        }
        return stringBuilder.toString();
    }
}
