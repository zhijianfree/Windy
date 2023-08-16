package com.zj.master.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.ResultEvent;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Slf4j
@Service
public class ClientNotifyService {

  private final Map<String, INotifyEvent> notifyEventMap;

  public ClientNotifyService(List<INotifyEvent> notifyEventList) {
    this.notifyEventMap = notifyEventList.stream()
        .collect(Collectors.toMap(event -> event.type().name(), event -> event));
  }

  public Boolean notifyEvent(ResultEvent resultEvent) {
    log.info("master receive notify = {}", JSON.toJSONString(resultEvent));
    String notifyType = resultEvent.getNotifyType().name();
    INotifyEvent notifyEvent = notifyEventMap.get(notifyType);
    if (Objects.isNull(notifyEvent)) {
      log.info("can not find notify type event");
      return false;
    }
    return notifyEvent.handle(resultEvent);
  }
}
