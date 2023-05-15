package com.zj.common.generate;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
