package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.service.CodeBuildService;
import com.zj.common.enums.ExecuteType;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class BuildCodeInvoker implements IRemoteInvoker {

  private final CodeBuildService codeBuildService;

  public BuildCodeInvoker(CodeBuildService codeBuildService) {
    this.codeBuildService = codeBuildService;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.BUILD;
  }

  @Override
  public void triggerRun(RequestContext requestContext, TaskNode taskNode) throws Exception {
    BuildParam buildParam = JSON.parseObject(JSON.toJSONString(requestContext.getData()), BuildParam.class);
    buildParam.setRecordId(taskNode.getRecordId());
    codeBuildService.buildCode(buildParam);
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    ResponseModel recordStatus = codeBuildService.getRecordStatus(taskNode.getRecordId());
    return JSON.toJSONString(recordStatus);
  }
}
