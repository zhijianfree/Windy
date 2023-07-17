package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.feature.PluginInfoDto;
import com.zj.master.service.PluginsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@RestController
@RequestMapping("/v1/devops/master")
public class PluginsUploadRest {

  private PluginsService pluginsService;

  public PluginsUploadRest(PluginsService pluginsService) {
    this.pluginsService = pluginsService;
  }

  @GetMapping(value = "/plugins")
  public ResponseMeta<List<PluginInfoDto>> getPlugins() {
    return new ResponseMeta(ErrorCode.SUCCESS, pluginsService.getPlugins());
  }

}
