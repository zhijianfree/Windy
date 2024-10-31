package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.dto.CodeBuildParamDto;
import com.zj.client.entity.vo.ApprovalParameter;
import com.zj.client.entity.vo.NodeRecord;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.ResponseMeta;
import com.zj.common.monitor.RequestProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 审批节点处理
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class ApprovalTrigger implements INodeTrigger {

  public static final String MESSAGE_SUCCESS_TIPS = "审批通过";
  public static final String MESSAGE_WAIT_TIPS = "审批通过";
  private final RequestProxy requestProxy;

  public ApprovalTrigger(RequestProxy requestProxy) {
    this.requestProxy = requestProxy;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.APPROVAL;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) {
    log.info("approval trigger run, no need to do");
    ApprovalParameter approvalParameter = JSON.parseObject(JSON.toJSONString(triggerContext.getData()), ApprovalParameter.class);
    taskNode.setExpireTime(approvalParameter.getMaxWait() * 1000);
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    //审批通过就直接根据数据库的状态即可，因为这个状态变化不在节点执行是用户在ui界面完成
    String result = requestProxy.getApprovalRecord(taskNode.getRecordId());
    ResponseMeta responseMeta = JSONObject.parseObject(result, ResponseMeta.class);
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(responseMeta.getData()),
        NodeRecord.class);
    log.info("get approval record recordId ={} status={}", taskNode.getRecordId(), nodeRecord.getStatus());
    QueryResponseModel responseModel = new QueryResponseModel();
    List<String> messageList = getMessageList(nodeRecord);
    responseModel.setMessage(messageList);
    responseModel.setStatus(nodeRecord.getStatus());
    responseModel.setData(nodeRecord);
    return responseModel;
  }

  private List<String> getMessageList(NodeRecord nodeRecord) {
      return Optional.ofNullable(nodeRecord.getResult()).filter(StringUtils::isNoneBlank).map(string -> JSON.parseArray(string,
            String.class)).orElseGet(() -> Collections.singletonList(ProcessStatus.exchange(nodeRecord.getStatus()).getDesc()));
  }
}
