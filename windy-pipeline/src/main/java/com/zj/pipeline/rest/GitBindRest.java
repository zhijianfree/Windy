package com.zj.pipeline.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.ResponseMeta;
import com.zj.pipeline.entity.dto.GitBindDto;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.service.GitBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author falcon
 * @since 2021/10/15
 */

@Slf4j
@RequestMapping("/v1/devops/pipeline")
@RestController
public class GitBindRest {

  @Autowired
  private GitBindService gitBindService;

  @ResponseBody
  @PostMapping("/git/bind")
  public ResponseMeta<String> createGitBind(@Validated @RequestBody GitBindDto gitBindDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        gitBindService.createGitBind(gitBindDto));
  }

  @ResponseBody
  @GetMapping("/{pipelineId}/git/binds")
  public ResponseMeta<List<GitBindDto>> listGitBinds(
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<List<GitBindDto>>(ErrorCode.SUCCESS,
        gitBindService.listGitBinds(pipelineId));
  }

  @ResponseBody
  @PutMapping("/git/bind")
  public ResponseMeta<Boolean> updatePipeline(@RequestBody GitBindDto gitBindDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        gitBindService.updateGitBind(gitBindDto));
  }

  @ResponseBody
  @DeleteMapping("/{pipelineId}/git/bind/{bindId}")
  public ResponseMeta<Boolean> deletePipeline(@PathVariable("bindId") String bindId,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        gitBindService.deleteGitBind(pipelineId,bindId));
  }

  @GetMapping("/{serviceId}/branches")
  public ResponseMeta<List<String>> getServiceBranch(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<List<String>>(ErrorCode.SUCCESS, gitBindService.getServiceBranch(serviceId));
  }

  @PostMapping("/web/hook")
  public void codeWebHook(@RequestBody JSONObject data) {
    gitBindService.notifyHook(data);
  }
}
