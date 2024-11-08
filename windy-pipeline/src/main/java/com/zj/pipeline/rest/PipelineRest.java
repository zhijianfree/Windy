package com.zj.pipeline.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.pipeline.entity.dto.PipelineDto;
import com.zj.pipeline.service.PipelineService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@RequestMapping("/v1/devops/pipeline")
@RestController
public class PipelineRest {

    private final PipelineService pipelineService;

    public PipelineRest(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @ResponseBody
    @GetMapping("/detail/{pipelineId}")
    public ResponseMeta<PipelineBO> queryPipeline(@PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.getPipelineDetail(pipelineId));
    }

    @ResponseBody
    @PostMapping("")
    public ResponseMeta<String> createPipeline(@Validated @RequestBody PipelineDto pipelineDto) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, pipelineService.createPipeline(pipelineDto));
    }

    @ResponseBody
    @PutMapping("/{service}/{pipelineId}")
    public ResponseMeta<Boolean> updatePipeline(@PathVariable("service") String service,
                                                @PathVariable("pipelineId") String pipelineId,
                                                @RequestBody PipelineDto pipelineDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
                pipelineService.updatePipeline(service, pipelineId, pipelineDto));
    }

    @ResponseBody
    @GetMapping("/{serviceId}/list")
    public ResponseMeta<List<PipelineBO>> listPipelines(
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
    public ResponseMeta<String> execute(@PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, pipelineService.execute(pipelineId));
    }

    @ResponseBody
    @PutMapping("/{historyId}/pause")
    public ResponseMeta<Boolean> pause(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, pipelineService.pause(historyId));
    }
}
