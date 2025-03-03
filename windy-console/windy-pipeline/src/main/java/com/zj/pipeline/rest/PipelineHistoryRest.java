package com.zj.pipeline.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.PipelineExecuteInfo;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.pipeline.service.PipelineHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@RequestMapping("/v1/devops/pipeline")
@RestController
public class PipelineHistoryRest {

    private final PipelineHistoryService historyService;

    public PipelineHistoryRest(PipelineHistoryService historyService) {
        this.historyService = historyService;
    }

    @ResponseBody
    @GetMapping("/history/{historyId}")
    public ResponseMeta<PipelineHistoryBO> queryPipelineHistory(@Validated @NotNull @PathVariable("historyId") String historyId) {
        return new ResponseMeta<PipelineHistoryBO>(ErrorCode.SUCCESS, historyService.getPipelineHistory(historyId));
    }

    @ResponseBody
    @PostMapping("/history")
    public ResponseMeta<String> createPipelineHistory(
            @NotNull @Validated @RequestBody PipelineHistoryBO pipelineHistoryBO) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, historyService.createPipelineHistory(pipelineHistoryBO));
    }

    @ResponseBody
    @GetMapping("/{pipelineId}/histories")
    public ResponseMeta<List<PipelineHistoryBO>> listPipelineHistories(@NotNull @PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<List<PipelineHistoryBO>>(ErrorCode.SUCCESS, historyService.listPipelineHistories(pipelineId));
    }

    @ResponseBody
    @GetMapping("/{service}/{pipelineId}/latest/history")
    public ResponseMeta<PipelineHistoryBO> getLatestPipelineHistory(@PathVariable("service") String service,
                                                                    @PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<PipelineHistoryBO>(ErrorCode.SUCCESS,
                historyService.getLatestPipelineHistory(service, pipelineId));
    }

    @ResponseBody
    @GetMapping("/histories/{historyId}/status")
    public ResponseMeta<PipelineExecuteInfo> getPipeLineStatusDetail(@NotNull @PathVariable("historyId") String historyId) {
        return new ResponseMeta<PipelineExecuteInfo>(ErrorCode.SUCCESS, historyService.getPipeLineStatusDetail(historyId));
    }
}
