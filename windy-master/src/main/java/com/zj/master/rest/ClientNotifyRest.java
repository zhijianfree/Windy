package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.master.service.ClientNotifyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@RestController
@RequestMapping("/v1/devops/dispatch")
public class ClientNotifyRest {

  private final ClientNotifyService clientNotifyService;

  public ClientNotifyRest(ClientNotifyService clientNotifyService) {
    this.clientNotifyService = clientNotifyService;
  }

  @PostMapping("/notify")
  public ResponseMeta<Boolean> notifyEvent(@RequestBody ResultEvent resultEvent) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, clientNotifyService.notifyEvent(resultEvent));
  }

}
