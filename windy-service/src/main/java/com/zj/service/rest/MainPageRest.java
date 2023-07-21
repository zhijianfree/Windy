package com.zj.service.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.service.entity.StaticsCount;
import com.zj.service.service.MainPageService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/7/21
 */
@RestController
@RequestMapping("/v1/devops")
public class MainPageRest {

  private MainPageService mainPageService;

  public MainPageRest(MainPageService mainPageService) {
    this.mainPageService = mainPageService;
  }

  @ResponseBody
  @GetMapping("/statics")
  public ResponseMeta<StaticsCount> queryStaticsCount() {
    return new ResponseMeta<StaticsCount>(ErrorCode.SUCCESS, mainPageService.queryStaticsCount());
  }
}
