package com.zj.common.adapter.uuid;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author guyuelan
 * @since 2023/1/29
 */
@Component
public class UniqueIdService {

  public String getUniqueId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
