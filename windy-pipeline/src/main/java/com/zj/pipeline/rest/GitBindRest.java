package com.zj.pipeline.rest;

import com.alibaba.fastjson.JSONObject;
import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.pipeline.service.GitBindService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @author guyuelan
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
  public ResponseMeta<String> createGitBind(@Validated @RequestBody BindBranchDto bindBranchDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        gitBindService.createGitBind(bindBranchDto));
  }

  @ResponseBody
  @GetMapping("/{pipelineId}/git/binds")
  public ResponseMeta<List<BindBranchDto>> listGitBinds(
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<List<BindBranchDto>>(ErrorCode.SUCCESS,
        gitBindService.listGitBinds(pipelineId));
  }

  @ResponseBody
  @PutMapping("/git/bind")
  public ResponseMeta<Boolean> updatePipeline(@RequestBody BindBranchDto bindBranchDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        gitBindService.updateGitBind(bindBranchDto));
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
