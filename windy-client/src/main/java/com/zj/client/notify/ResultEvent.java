package com.zj.client.notify;

import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/16
 */
@Data
@Builder
public class ResultEvent {
  private String masterIP;
  private String executeId;
  private NotifyType notifyType;
  private ProcessStatus status;
  private Object object;
}
