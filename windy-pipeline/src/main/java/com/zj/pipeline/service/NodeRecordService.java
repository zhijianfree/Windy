package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.entity.po.pipeline.PipelineHistory;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.pipeline.executer.vo.PipelineRecord;
import com.zj.pipeline.executer.vo.TaskNodeRecord;
import com.zj.domain.mapper.pipeline.NodeRecordMapper;
import com.zj.domain.mapper.pipeline.PipelineHistoryMapper;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService extends ServiceImpl<NodeRecordMapper, NodeRecord> {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  public static final String APPROVAL_TIPS = "审核通过";
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
    nodeRecordRepository.saveNodeRecord(nodeRecord);
  }

  public void updateNodeRecord(TaskNodeRecord taskNodeRecord) {
    log.info("update task node status taskId={}", taskNodeRecord.getRecordId());
    NodeRecord nodeRecord = JSON.parseObject(JSON.toJSONString(taskNodeRecord), NodeRecord.class);
    boolean update = nodeRecordRepository.updateNodeRecord(nodeRecord);
    log.info("update node record status={}", update);

  }

  public void updateNodeRecordStatus(String recordId, Integer type, String message) {
    boolean update = nodeRecordRepository.updateNodeRecordStatus(recordId, type, message);
    log.info("update result={}", update);
  }

  public void batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus) {
    boolean batchUpdate = nodeRecordRepository.batchUpdateStatus(recordIds, processStatus);
    log.info("batch update record status={}", batchUpdate);
  }

  public Boolean approval(String historyId, String nodeId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getNodeId, nodeId));
    log.info("get approval recordId={}", nodeRecord.getNodeId());
    updateNodeRecordStatus(nodeRecord.getRecordId(), ProcessStatus.SUCCESS.getType(),
        JSON.toJSONString(Collections.singletonList(APPROVAL_TIPS)));
    return true;
  }
}
