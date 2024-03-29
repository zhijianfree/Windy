package com.zj.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
public class IpUtils {

  public static String getLocalIP(){
    try {
      InetAddress inetAddress = findFirstNonLoopbackAddress();
      return inetAddress.getHostAddress();
    } catch (Exception e) {
      log.info("error get local ip", e);
    }
    return "unknown";
  }

  public static String getHostName(){
    try {
      InetAddress inetAddress = findFirstNonLoopbackAddress();
      return inetAddress.getHostName();
    } catch (Exception e) {
      log.info("error get local ip", e);
    }
    return "unknown";
  }

  /**
   * 此处使用的Eureka获取IP的方式
   * */
  private static InetAddress findFirstNonLoopbackAddress() {
    InetAddress result = null;
    try {
      int lowest = Integer.MAX_VALUE;
      for (Enumeration<NetworkInterface> nics = NetworkInterface
          .getNetworkInterfaces(); nics.hasMoreElements();) {
        NetworkInterface ifc = nics.nextElement();
        if (ifc.isUp()) {
          if (ifc.getIndex() < lowest || result == null) {
            lowest = ifc.getIndex();
          }
          else {
            continue;
          }

          for (Enumeration<InetAddress> addrs = ifc
              .getInetAddresses(); addrs.hasMoreElements();) {
            InetAddress address = addrs.nextElement();
            if (address instanceof Inet4Address
                && !address.isLoopbackAddress()) {
              result = address;
            }
          }
        }
      }
    }
    catch (IOException ex) {
      log.error("Cannot get first non-loopback address", ex);
    }

    return Optional.ofNullable(result).orElseGet(() -> {
      try {
        return InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        log.warn("Unable to retrieve localhost");
      }
      return null;
    });
  }
}
