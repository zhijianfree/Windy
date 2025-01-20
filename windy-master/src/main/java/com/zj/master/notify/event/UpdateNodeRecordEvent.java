package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.dispatch.pipeline.PipelineExecuteProxy;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class UpdateNodeRecordEvent implements INotifyEvent {

  private final INodeRecordRepository nodeRecordRepository;
  private final PipelineExecuteProxy pipelineExecuteProxy;
  private final ISubDispatchLogRepository subTaskLogRepository;

  public UpdateNodeRecordEvent(INodeRecordRepository nodeRecordRepository,
      PipelineExecuteProxy pipelineExecuteProxy, ISubDispatchLogRepository subTaskLogRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.pipelineExecuteProxy = pipelineExecuteProxy;
    this.subTaskLogRepository = subTaskLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    String string = JSON.toJSONString(resultEvent.getParams());
    log.info("receive node record update event id = {} event={} status={}", resultEvent.getExecuteId(),
        resultEvent.getExecuteType(), resultEvent.getStatus());
    NodeRecordBO oldNodeRecord = nodeRecordRepository.getRecordById(resultEvent.getExecuteId());
    if (ProcessStatus.isCompleteStatus(oldNodeRecord.getStatus()) && !Objects.equals(
        ExecuteType.APPROVAL.name(), resultEvent.getExecuteType())) {
      //如果任务已经是完结状态，不能再更新记录
      //审批节点完成需要由负责人在console界面操作而不是在通知中完成，所以审批完成可以继续执行下一个节点
      log.info("node record status completed, not update recordId={}", oldNodeRecord.getRecordId());
      return true;
    }

    NodeRecordBO nodeRecord = JSON.parseObject(string, NodeRecordBO.class);
    nodeRecord.setRecordId(resultEvent.getExecuteId());
    nodeRecord.setStatus(resultEvent.getStatus().getType());
    Optional.ofNullable(resultEvent.getContext()).ifPresent(
        context -> nodeRecord.setPipelineContext(resultEvent.getContext()));

    boolean updateStatus = nodeRecordRepository.updateNodeRecord(nodeRecord);
    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), nodeRecord.getNodeId(),
        nodeRecord.getStatus());

    //单个节点状态变化要通知给执行者，然后执行一下节点的任务
    pipelineExecuteProxy.statusChange(nodeRecord);
    return updateStatus;
  }
}
