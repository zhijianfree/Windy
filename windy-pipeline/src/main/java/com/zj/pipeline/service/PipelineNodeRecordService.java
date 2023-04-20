package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class PipelineNodeRecordService extends ServiceImpl<NodeRecordMapper, NodeRecord> {

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
    pipelineHistory.setCreateTime(System.currentTimeMillis());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    historyMapper.insert(pipelineHistory);
  }

  public void saveTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("save task node record taskId={}", taskNodeRecord.getRecordId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    nodeRecord.setCreateTime(System.currentTimeMillis());
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    save(nodeRecord);
  }

  public void updateTaskNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("update task node status taskId={}", taskNodeRecord.getRecordId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getRecordId, taskNodeRecord.getRecordId()));
  }

  public void updateTaskNodeStatus(String recordId, Integer type) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(type);
    update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
  }

  public NodeRecord getNodeRecord(String recordId) {
    return getOne(Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
  }
}
