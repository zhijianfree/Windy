package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.dto.CodeBuildParamDto;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.service.CodeBuildService;
import com.zj.common.enums.ExecuteType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 代码构建处理
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
    CodeBuildParamDto codeBuildParamDto = JSON.parseObject(JSON.toJSONString(triggerContext.getData()), CodeBuildParamDto.class);
    codeBuildParamDto.setRecordId(taskNode.getRecordId());
    codeBuildService.buildCode(codeBuildParamDto, taskNode);
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    QueryResponseModel recordStatus = codeBuildService.getRecordStatus(taskNode.getRecordId());
    return JSON.toJSONString(recordStatus);
  }
}
