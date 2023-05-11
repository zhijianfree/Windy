package com.zj.common.generate;

import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/1/29
 */
@Service
public class UniqueIdService {

  public String getUniqueId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
