package com.zj.client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExceptionUtils {

  private static final String LINE_SEPARATOR = "\r\n";

  public static List<String> getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return msg;
  }

  public static String getSimplifyError(Throwable e) {
    return getExceptionStackTrace(e);
  }

  private static String getExceptionStackTrace(Throwable e) {
    if (Objects.isNull(e)) {
      return "";
    }

    String message = e.toString();
    StringBuilder stringBuilder = null;
    if (message.length() > 256) {
      stringBuilder = new StringBuilder(
          message.substring(message.length() - 256, message.length() - 1));
    } else {
      stringBuilder = new StringBuilder(message);
    }

    StackTraceElement[] elements = e.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
      stringBuilder.append(LINE_SEPARATOR).append("\tat");
      if (elements[i].toString().length() > 256) {
        stringBuilder.append(elements[i].toString(), 0, 255);
      } else {
        stringBuilder.append(elements[i].toString());
      }
    }
    return stringBuilder.toString();
  }
}
