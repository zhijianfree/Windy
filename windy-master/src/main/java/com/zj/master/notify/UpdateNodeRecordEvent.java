package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.dispatch.pipeline.PipelineExecuteProxy;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class UpdateNodeRecordEvent implements INotifyEvent {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private PipelineExecuteProxy pipelineExecuteProxy;

  @Autowired
  private ISubDispatchLogRepository subTaskLogRepository;

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    String string = JSON.toJSONString(resultEvent.getParams());
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        string);
    NodeRecordDto record = nodeRecordRepository.getRecordById(resultEvent.getExecuteId());
    if (ProcessStatus.isCompleteStatus(record.getStatus()) && !Objects.equals(
        ExecuteType.APPROVAL.name(), resultEvent.getExecuteType())) {
      log.info("node record status completed,not update recordId={}", record.getRecordId());
      return true;
    }

    NodeRecordDto nodeRecord = JSON.parseObject(string, NodeRecordDto.class);
    nodeRecord.setRecordId(resultEvent.getExecuteId());
    nodeRecord.setStatus(resultEvent.getStatus().getType());
    boolean updateStatus = nodeRecordRepository.updateNodeRecord(nodeRecord);

    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), nodeRecord.getNodeId(),
        nodeRecord.getStatus());
    //单个节点状态变化要通知给执行者，然后执行一下节点的任务
    pipelineExecuteProxy.statusChange(nodeRecord);
    return updateStatus;
  }
}
