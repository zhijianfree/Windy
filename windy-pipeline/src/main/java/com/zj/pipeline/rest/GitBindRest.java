package com.zj.pipeline.rest;

import com.zj.common.adapter.git.CommitMessage;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.pipeline.service.GitBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@Slf4j
@RequestMapping("/v1/devops/pipeline")
@RestController
public class GitBindRest {

  private final GitBindService gitBindService;

  public GitBindRest(GitBindService gitBindService) {
    this.gitBindService = gitBindService;
  }

  @ResponseBody
  @GetMapping("/services/{serviceId}/{branchName}/commits")
  public ResponseMeta<List<CommitMessage>> getBranchCommits(@PathVariable("branchName") String branchName,
                                                            @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<List<CommitMessage>>(ErrorCode.SUCCESS, gitBindService.getBranchCommits(serviceId, branchName));
  }
  @ResponseBody
  @PostMapping("/git/bind")
  public ResponseMeta<String> createGitBind(@Validated @RequestBody BindBranchBO bindBranchBO) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, gitBindService.createGitBind(bindBranchBO));
  }

  @ResponseBody
  @GetMapping("/{pipelineId}/git/binds")
  public ResponseMeta<List<BindBranchBO>> listGitBinds(
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<List<BindBranchBO>>(ErrorCode.SUCCESS, gitBindService.listGitBinds(pipelineId));
  }

  @ResponseBody
  @PutMapping("/git/bind")
  public ResponseMeta<Boolean> updatePipeline(@RequestBody BindBranchBO bindBranchBO) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, gitBindService.updateGitBind(bindBranchBO));
  }

  @ResponseBody
  @DeleteMapping("/{pipelineId}/git/bind/{bindId}")
  public ResponseMeta<Boolean> deletePipeline(@PathVariable("bindId") String bindId,
      @PathVariable("pipelineId") String pipelineId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, gitBindService.deleteGitBind(pipelineId,bindId));
  }

  @GetMapping("/{serviceId}/branches")
  public ResponseMeta<List<String>> getServiceBranch(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<List<String>>(ErrorCode.SUCCESS, gitBindService.getServiceBranch(serviceId));
  }

  @PostMapping("/web/hook")
  public ResponseMeta<Boolean> codeWebHook(@RequestBody Object data, @RequestParam("platform") String platform,
                                           HttpServletRequest request) {
    return new ResponseMeta(ErrorCode.SUCCESS, gitBindService.notifyHook(data, platform, request));
  }
}
