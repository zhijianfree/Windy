package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.dispatch.pipeline.PipelineExecuteProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Service
public class UpdateNodeRecordEvent implements INotifyEvent{

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private PipelineExecuteProxy pipelineExecuteProxy;

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_NODE_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    String string = JSON.toJSONString(resultEvent.getParams());
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(), string);

    NodeRecordDto nodeRecord = JSON.parseObject(string, NodeRecordDto.class);
    nodeRecord.setRecordId(resultEvent.getExecuteId());
    nodeRecord.setStatus(resultEvent.getStatus().getType());
    boolean updateStatus = nodeRecordRepository.updateNodeRecord(nodeRecord);

    //单个节点状态变化要通知给执行者，然后执行一下节点的任务
    pipelineExecuteProxy.statusChange(nodeRecord);
    return updateStatus;
  }
}
