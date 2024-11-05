package com.zj.client.rest;

import com.zj.common.entity.service.LanguageVersionDto;
import com.zj.client.service.ClientCollector;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.ResponseMeta;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@RestController
@RequestMapping("/v1/devops/client")
public class ClientCollectorRest {

  private final ClientCollector clientCollector;

  public ClientCollectorRest(ClientCollector clientCollector) {
    this.clientCollector = clientCollector;
  }

  @GetMapping("/instance")
  public ResponseMeta<ClientCollectDto> getInstance() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, clientCollector.getInstanceInfo());
  }

  @GetMapping("/languages/version")
  public ResponseMeta<LanguageVersionDto> getLanguageVersions() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, clientCollector.getLanguageVersions());
  }

}
