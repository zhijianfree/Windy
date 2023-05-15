package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.PipelineExecuteInfo;
import com.zj.pipeline.service.PipelineHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@RequestMapping("/v1/devops/pipeline")
@RestController
public class PipelineHistoryRest {

  @Autowired
  private PipelineHistoryService historyService;

  @ResponseBody
  @GetMapping("/history/{historyId}")
  public ResponseMeta<PipelineHistoryDto> queryPipelineHistory(
      @NotNull @PathVariable("historyId") String historyId) {
    return new ResponseMeta<PipelineHistoryDto>(ErrorCode.SUCCESS,
        historyService.getPipelineHistory(historyId));
  }

  @ResponseBody
  @PostMapping("/history")
  public ResponseMeta<String> createPipelineHistory(
      @NotNull @Validated @RequestBody PipelineHistoryDto pipelineHistoryDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        historyService.createPipelineHistory(pipelineHistoryDto));
  }

  @ResponseBody
  @GetMapping("/{pipelineId}/histories")
  public ResponseMeta<List<PipelineHistoryDto>> listPipelineHistories(
      @NotNull @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<List<PipelineHistoryDto>>(ErrorCode.SUCCESS,
        historyService.listPipelineHistories(pipelineId));
  }

  @ResponseBody
  @GetMapping("/{service}/{pipelineId}/latest/history")
  public ResponseMeta<PipelineHistoryDto> getLatestPipelineHistory(
      @PathVariable("service") String service,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<PipelineHistoryDto>(ErrorCode.SUCCESS,
        historyService.getLatestPipelineHistory(pipelineId));
  }

  @ResponseBody
  @GetMapping("/{historyId}/status")
  public ResponseMeta<PipelineExecuteInfo> getPipeLineStatusDetail(
      @NotNull @PathVariable("historyId") String historyId) {
    return new ResponseMeta<PipelineExecuteInfo>(ErrorCode.SUCCESS,
        historyService.getPipeLineStatusDetail(historyId));
  }
}
