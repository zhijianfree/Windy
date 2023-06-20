package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.vo.NodeRecord;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;
import com.zj.common.monitor.RequestProxy;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class ApprovalInvoker implements IRemoteInvoker {

  public static final String MESSAGE_TIPS = "审批结果";
  @Autowired
  private RequestProxy requestProxy;

  @Override
  public ExecuteType type() {
    return ExecuteType.APPROVAL;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, TaskNode taskNode) throws IOException {
    return true;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    //审批通过就直接根据数据库的状态即可，因为这个状态变化不在节点执行是用户在ui界面完成
    String result = requestProxy.getApprovalRecord(taskNode.getRecordId());
    JSONObject jsonObject = JSONObject.parseObject(result, JSONObject.class);
    NodeRecord record = JSON.parseObject(JSON.toJSONString(jsonObject.get("data")), NodeRecord.class);
    log.info("get approval record recordId ={} status={}", taskNode.getRecordId(), record.getStatus());
    QueryResponseModel responseModel = new QueryResponseModel();
    responseModel.setMessage(Collections.singletonList(MESSAGE_TIPS));
    responseModel.setStatus(record.getStatus());
    responseModel.setData(jsonObject.getJSONObject("data"));
    return JSON.toJSONString(responseModel);
  }
}
