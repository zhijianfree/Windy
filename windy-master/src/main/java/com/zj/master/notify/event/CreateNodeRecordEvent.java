package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class CreateNodeRecordEvent implements INotifyEvent {

  private final INodeRecordRepository nodeRecordRepository;
  private final ISubDispatchLogRepository subDispatchLogRepository;

  public CreateNodeRecordEvent(INodeRecordRepository nodeRecordRepository,
      ISubDispatchLogRepository subDispatchLogRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
    this.subDispatchLogRepository = subDispatchLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    NodeRecordDto nodeRecord = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()),
        NodeRecordDto.class);

    subDispatchLogRepository.updateSubLogClientIp(resultEvent.getLogId(), nodeRecord.getNodeId(),
        resultEvent.getClientIp());

    return nodeRecordRepository.saveNodeRecord(nodeRecord);
  }
}
