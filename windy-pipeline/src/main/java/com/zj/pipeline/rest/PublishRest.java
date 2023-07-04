package com.zj.pipeline.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.pipeline.service.PublishService;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class PublishRest {

  private PublishService publishService;

  public PublishRest(PublishService publishService) {
    this.publishService = publishService;
  }

  @PostMapping("/publish")
  public ResponseMeta<Boolean> createPublish(@Validated @RequestBody PublishBindDto publishBind) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, publishService.createPublish(publishBind));
  }

  @PutMapping("/publish")
  public ResponseMeta<Boolean> updatePublish(@RequestBody PublishBindDto publishBind) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, publishService.updatePublish(publishBind));
  }

  @GetMapping("/{serviceId}/publishes")
  public ResponseMeta<List<PublishBindDto>> getPublishes(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, publishService.getPublishes(serviceId));
  }

  @DeleteMapping("/publish/{publishId}")
  public ResponseMeta<Boolean> deletePublish(@PathVariable("publishId") String publishId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, publishService.deletePublish(publishId));
  }
}
