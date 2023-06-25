package com.zj.client.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.TriggerContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.service.CodeBuildService;
import com.zj.common.enums.ExecuteType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class BuildCodeTrigger implements INodeTrigger {

  private final CodeBuildService codeBuildService;

  public BuildCodeTrigger(CodeBuildService codeBuildService) {
    this.codeBuildService = codeBuildService;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.BUILD;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws Exception {
    BuildParam buildParam = JSON.parseObject(JSON.toJSONString(triggerContext.getData()), BuildParam.class);
    buildParam.setRecordId(taskNode.getRecordId());
    codeBuildService.buildCode(buildParam);
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    ResponseModel recordStatus = codeBuildService.getRecordStatus(taskNode.getRecordId());
    return JSON.toJSONString(recordStatus);
  }
}
