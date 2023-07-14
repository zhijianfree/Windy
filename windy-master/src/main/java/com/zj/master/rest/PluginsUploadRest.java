package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.master.service.PluginsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

/**
 * @author falcon
 * @since 2023/7/14
 */
@RestController
@RequestMapping("/v1/devops/master")
public class PluginsUploadRest {

  private PluginsService pluginsService;

  @PostMapping(value = "/plugins/upload")
  public ResponseMeta<Boolean> uploadPlugins(@RequestPart("file") MultipartFile file) {
    return new ResponseMeta(ErrorCode.SUCCESS, pluginsService.uploadTemplate(file));
  }

}
