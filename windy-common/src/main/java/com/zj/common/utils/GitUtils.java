package com.zj.common.utils;

public class GitUtils {
  public static String getServiceFromUrl(String gitUrl) {
    if (org.apache.commons.lang.StringUtils.isBlank(gitUrl)) {
      return null;
    }
    String[] strings = gitUrl.split("/");
    return strings[strings.length - 1].split("\\.")[0];
  }
}
