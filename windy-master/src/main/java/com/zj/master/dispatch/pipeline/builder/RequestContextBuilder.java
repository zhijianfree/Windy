package com.zj.master.dispatch.pipeline.builder;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.master.entity.vo.ApprovalContext;
import com.zj.master.entity.vo.BuildCodeContext;
import com.zj.master.entity.vo.ConfigDetail;
import com.zj.master.entity.vo.DeployContext;
import com.zj.master.entity.vo.FeatureContext;
import com.zj.master.entity.vo.HttpContext;
import com.zj.master.entity.vo.MergeMasterContext;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.WaitContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
public class RequestContextBuilder {

  private static final Map<String, Function<ActionDetail, RequestContext>> factoryMap = new HashMap<>();

  static {
    factoryMap.put(ExecuteType.WAIT.name(), RequestContextBuilder::buildWaitContext);
    factoryMap.put(ExecuteType.HTTP.name(), RequestContextBuilder::buildHttpContext);
    factoryMap.put(ExecuteType.TEST.name(), RequestContextBuilder::buildTestContext);
    factoryMap.put(ExecuteType.DEPLOY.name(), RequestContextBuilder::buildDeployContext);
    factoryMap.put(ExecuteType.APPROVAL.name(), RequestContextBuilder::buildApprovalContext);
    factoryMap.put(ExecuteType.BUILD.name(), RequestContextBuilder::buildCodeContext);
    factoryMap.put(ExecuteType.MERGE.name(), RequestContextBuilder::buildMergeContext);
  }

  public static RequestContext createContext(ActionDetail actionDetail) {
    PipelineActionDto action = actionDetail.getAction();
    Function<ActionDetail, RequestContext> contextFunc = factoryMap.get(action.getExecuteType());
    if (Objects.isNull(contextFunc)) {
      log.info("can not find context function type={}", action.getExecuteType());
      return new RequestContext();
    }
    return contextFunc.apply(actionDetail);
  }

  private static RequestContext buildWaitContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), WaitContext.class);
  }

  private static RequestContext buildApprovalContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()),
        ApprovalContext.class);
  }


  private static RequestContext buildHttpContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    Map<String, String> requestContext = configDetail.getParamList();
    return HttpContext.builder().body(JSON.toJSONString(requestContext))
        .url(actionDetail.getAction().getActionUrl()).build();
  }

  private static RequestContext buildTestContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()),
        FeatureContext.class);
  }

  private static RequestContext buildDeployContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()),
        DeployContext.class);
  }

  private static RequestContext buildCodeContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()),
        BuildCodeContext.class);
  }

  private static RequestContext buildMergeContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()),
        MergeMasterContext.class);
  }


}
