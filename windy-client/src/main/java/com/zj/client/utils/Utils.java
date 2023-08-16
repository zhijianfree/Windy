package com.zj.client.utils;

public class Utils {
  public static String getServiceFromUrl(String gitUrl) {
    if (org.apache.commons.lang.StringUtils.isBlank(gitUrl)) {
      return null;
    }
    String[] strings = gitUrl.split("/");
    return strings[strings.length - 1].split("\\.")[0];
  }
}
