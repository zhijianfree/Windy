package com.zj.pipeline.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.service.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@RequestMapping("/v1/devops/pipeline")
@RestController
public class PipelineRest {

  @Autowired
  private PipelineService pipelineService;

  @ResponseBody
  @GetMapping("/{service}/{pipelineId}")
  public ResponseMeta<PipelineDTO> queryPipeline(@PathVariable("service") String service,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.getPipelineDetail(pipelineId));
  }

  @ResponseBody
  @PostMapping("")
  public ResponseMeta<String> createPipeline(@Validated @RequestBody PipelineDTO pipelineDTO) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        pipelineService.createPipeline(pipelineDTO));
  }

  @ResponseBody
  @PutMapping("/{service}/{pipelineId}")
  public ResponseMeta<Boolean> updatePipeline(@PathVariable("service") String service,
      @PathVariable("pipelineId") String pipelineId,
      @RequestBody PipelineDTO pipelineDTO) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        pipelineService.updatePipeline(service, pipelineId, pipelineDTO));
  }

  @ResponseBody
  @GetMapping("/{serviceId}/list")
  public ResponseMeta<List<PipelineDTO>> listPipelines(
      @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.listPipelines(serviceId));
  }

  @ResponseBody
  @DeleteMapping("/{service}/{pipelineId}")
  public ResponseMeta<Boolean> deletePipeline(@PathVariable("service") String service,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS,
        pipelineService.deletePipeline(service, pipelineId));
  }

  @ResponseBody
  @PostMapping("/{pipelineId}")
  public ResponseMeta<Boolean> execute(@PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, pipelineService.execute(pipelineId));
  }

  @ResponseBody
  @PutMapping("/{historyId}/pause")
  public ResponseMeta<Boolean> pause(@PathVariable("historyId") String historyId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, pipelineService.pause(historyId));
  }
}
