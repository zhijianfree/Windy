package com.zj.client.rest;

import com.zj.client.service.ClientMonitor;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ClientCollect;
import com.zj.common.model.ResponseMeta;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@RestController
@RequestMapping("/v1/devops/client")
public class ClientMonitorRest {

  private ClientMonitor clientMonitor;

  public ClientMonitorRest(ClientMonitor clientMonitor) {
    this.clientMonitor = clientMonitor;
  }

  @GetMapping("/instance")
  public ResponseMeta<ClientCollect> getInstance() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, clientMonitor.getInstanceInfo());
  }
}
