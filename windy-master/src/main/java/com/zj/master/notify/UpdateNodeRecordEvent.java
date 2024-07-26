package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.dispatch.pipeline.PipelineExecuteProxy;
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
    log.info("receive node record update event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent.getExecuteType());
    NodeRecordDto oldNodeRecord = nodeRecordRepository.getRecordById(resultEvent.getExecuteId());
    if (ProcessStatus.isCompleteStatus(oldNodeRecord.getStatus()) && !Objects.equals(
        ExecuteType.APPROVAL.name(), resultEvent.getExecuteType())) {
      log.info("node record status completed,not update recordId={}", oldNodeRecord.getRecordId());
      return true;
    }

    NodeRecordDto nodeRecord = JSON.parseObject(string, NodeRecordDto.class);
    nodeRecord.setRecordId(resultEvent.getExecuteId());
    nodeRecord.setStatus(resultEvent.getStatus().getType());
    Optional.ofNullable(resultEvent.getContext()).ifPresent(
        context -> nodeRecord.setPipelineContext(JSON.toJSONString(resultEvent.getContext())));

    boolean updateStatus = nodeRecordRepository.updateNodeRecord(nodeRecord);
    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), nodeRecord.getNodeId(),
        nodeRecord.getStatus());

    //单个节点状态变化要通知给执行者，然后执行一下节点的任务
    pipelineExecuteProxy.statusChange(nodeRecord);
    return updateStatus;
  }
}
