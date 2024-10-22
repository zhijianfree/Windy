package com.zj.common.utils;

import org.apache.commons.lang.StringUtils;

public class GitUtils {
  public static String getServiceFromUrl(String gitUrl) {
    if (StringUtils.isBlank(gitUrl)) {
      return null;
    }
    String[] strings = gitUrl.split("/");
    return strings[strings.length - 1].split("\\.")[0];
  }
}
