package com.zj.client.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.vo.NodeRecord;
import com.zj.client.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.TriggerContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.monitor.RequestProxy;
import java.util.Collections;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class ApprovalTrigger implements INodeTrigger {

  public static final String MESSAGE_SUCCESS_TIPS = "审批通过";
  public static final String MESSAGE_WAIT_TIPS = "审批通过";
  @Autowired
  private RequestProxy requestProxy;

  @Override
  public ExecuteType type() {
    return ExecuteType.APPROVAL;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) {
    log.info("approval trigger run, no need to do");
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    //审批通过就直接根据数据库的状态即可，因为这个状态变化不在节点执行是用户在ui界面完成
    String result = requestProxy.getApprovalRecord(taskNode.getRecordId());
    JSONObject jsonObject = JSONObject.parseObject(result, JSONObject.class);
    NodeRecord record = JSON.parseObject(JSON.toJSONString(jsonObject.get("data")),
        NodeRecord.class);
    log.info("get approval record recordId ={} status={}", taskNode.getRecordId(),
        record.getStatus());
    QueryResponseModel responseModel = new QueryResponseModel();
    String msg =
        Objects.equals(record.getStatus(), ProcessStatus.SUCCESS.getType()) ? MESSAGE_SUCCESS_TIPS
            : MESSAGE_WAIT_TIPS;
    responseModel.setMessage(Collections.singletonList(msg));
    responseModel.setStatus(record.getStatus());
    responseModel.setData(jsonObject.getJSONObject("data"));
    return JSON.toJSONString(responseModel);
  }
}
