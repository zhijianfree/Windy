package com.zj.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Slf4j
public class IpUtils {

  public static String getLocalIP(){
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.info("error get local ip", e);
    }
    return "unknown";
  }
}
