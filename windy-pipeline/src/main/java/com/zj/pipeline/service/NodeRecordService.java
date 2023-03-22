package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.po.PipelineRecord;
import com.zj.pipeline.executer.po.TaskNodeRecord;
import com.zj.pipeline.mapper.NodeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService {

  @Autowired
  private NodeRecordMapper nodeRecordMapper;

  public void savePipelineRecord(PipelineRecord pipelineRecord) {
    log.info("save pipeline record recordId={}", pipelineRecord.getRecordId());
  }

  public void saveTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("save task node record taskId={}", taskNodeRecord.getTaskId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    nodeRecordMapper.insert(nodeRecord);
  }

  public void updateTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("update task node status taskId={}", taskNodeRecord.getTaskId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    nodeRecordMapper.update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getTaskId, taskNodeRecord.getTaskId()));
  }

  public void updateTaskStatus(String taskId, ProcessStatus success) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(success.getType());
    nodeRecord.setTaskId(taskId);
    nodeRecordMapper.update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getTaskId, taskId));
  }
}
