package com.zj.client.rest;

import com.zj.client.entity.BuildParam;
import com.zj.client.entity.ResponseModel;
import com.zj.client.service.CodeBuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@RestController
@RequestMapping("/v1/devops/client")
public class CodeBuildRest {

  @Autowired
  private CodeBuildService codeBuildService;

  @PostMapping("/build")
  public ResponseModel runBuild(@RequestBody BuildParam buildParam) {
    return new ResponseModel(codeBuildService.buildCode(buildParam), "构建中");
  }

  @PostMapping("/build/status")
  public ResponseModel buildStatus(@RequestBody BuildParam buildParam) {
    return new ResponseModel(codeBuildService.getRecordStatus(buildParam.getRecordId()),"查询结果");
  }
}
