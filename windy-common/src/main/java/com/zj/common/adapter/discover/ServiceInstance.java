package com.zj.common.adapter.discover;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/23
 */
@Data
public class ServiceInstance {

  private String serviceId;

  private String host;

  private String ip;

  private Integer port;
}
