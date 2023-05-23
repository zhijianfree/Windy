package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Service
public class CreateNodeRecordEvent implements INotifyEvent {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private ISubDispatchLogRepository subDispatchLogRepository;

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
