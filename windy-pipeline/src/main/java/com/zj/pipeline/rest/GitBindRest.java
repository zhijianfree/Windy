package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.pipeline.entity.dto.GitBindDto;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.service.GitBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author falcon
 * @since 2021/10/15
 */

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
  @PutMapping("/{pipelineId}/git/bind/{bindId}")
  public ResponseMeta<Integer> updatePipeline(@PathVariable("bindId") String bindId,
      @PathVariable("pipelineId") String pipelineId,
      @RequestBody GitBindDto gitBindDto) {
    return new ResponseMeta<Integer>(ErrorCode.SUCCESS,
        gitBindService.updateGitBind(pipelineId, bindId, gitBindDto));
  }

  @ResponseBody
  @DeleteMapping("/{pipelineId}/git/bind/{bindId}")
  public ResponseMeta<Integer> deletePipeline(@PathVariable("bindId") String bindId,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<Integer>(ErrorCode.SUCCESS,
        gitBindService.deleteGitBind(pipelineId,bindId));
  }
}
