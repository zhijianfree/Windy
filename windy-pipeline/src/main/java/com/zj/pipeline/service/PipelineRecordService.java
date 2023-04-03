package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.entity.po.PipelineHistory;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.po.PipelineRecord;
import com.zj.pipeline.executer.po.TaskNodeRecord;
import com.zj.pipeline.mapper.NodeRecordMapper;
import com.zj.pipeline.mapper.PipelineHistoryMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2022/5/24
 */
@Slf4j
@Component
public class PipelineRecordService {

  @Autowired
  private NodeRecordMapper nodeRecordMapper;

  @Autowired
  private PipelineHistoryMapper historyMapper;

  public void savePipelineHistory(PipelineRecord pipelineRecord) {
    log.info("save pipeline record recordId={}", pipelineRecord.getHistoryId());
    PipelineHistory pipelineHistory = JSON.parseObject(JSON.toJSONString(pipelineRecord),
        PipelineHistory.class);
    pipelineHistory.setPipelineConfig("");
    pipelineHistory.setPipelineStatus(ProcessStatus.RUNNING.getType());
    pipelineHistory.setBranch("master");
    pipelineHistory.setExecutor(pipelineRecord.getUserId());
    pipelineHistory.setHistoryId(UUID.randomUUID().toString().replace("-", ""));
    pipelineHistory.setCreateTime(System.currentTimeMillis());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    historyMapper.insert(pipelineHistory);
  }

  public void saveTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("save task node record taskId={}", taskNodeRecord.getRecordId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    nodeRecordMapper.insert(nodeRecord);
  }

  public void updateTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("update task node status taskId={}", taskNodeRecord.getRecordId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    nodeRecordMapper.update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getRecordId, taskNodeRecord.getRecordId()));
  }

  public void updateTaskStatus(String taskId, ProcessStatus success) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(success.getType());
    nodeRecord.setHistoryId(taskId);
    nodeRecordMapper.update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).eq(NodeRecord::getHistoryId, taskId));
  }
}
