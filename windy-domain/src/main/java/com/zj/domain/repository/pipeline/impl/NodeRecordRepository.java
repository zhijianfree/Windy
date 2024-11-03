package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.mapper.pipeline.NodeRecordMapper;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Repository
public class NodeRecordRepository extends ServiceImpl<NodeRecordMapper, NodeRecord> implements
    INodeRecordRepository {

  @Override
  public boolean saveNodeRecord(NodeRecordBO nodeRecordBO) {
    NodeRecord nodeRecord = convertNodeRecord(nodeRecordBO);
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
    nodeRecord.setCreateTime(dateNow);
    return save(nodeRecord);
  }

  @Override
  public boolean updateNodeRecord(NodeRecordBO nodeRecordBO) {
    NodeRecord nodeRecord = convertNodeRecord(nodeRecordBO);
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    return update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getRecordId, nodeRecord.getRecordId()));
  }

  @Override
  public boolean updateNodeRecordStatus(String recordId, Integer type, List<String> messageList) {
    NodeRecordBO nodeRecord = new NodeRecordBO();
    nodeRecord.setStatus(type);
    nodeRecord.setRecordId(recordId);
    nodeRecord.setResult(messageList);
    return updateNodeRecord(nodeRecord);
  }

  @Override
  public boolean batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(processStatus.getType());
    nodeRecord.setRecordResult(JSON.toJSONString(Collections.singleton(processStatus.getDesc())));
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    boolean batchUpdate = update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).in(NodeRecord::getRecordId, recordIds));
    log.info("batch update record status={}", batchUpdate);
    return batchUpdate;
  }

  @Override
  public List<NodeRecordBO> getRecordsByHistoryId(String historyId) {
    List<NodeRecord> nodeRecords = list(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId));
    return nodeRecords.stream().map(NodeRecordRepository::convertNodeRecordBO).collect(Collectors.toList());
  }

  @Override
  public NodeRecordBO getRecordById(String recordId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
    return convertNodeRecordBO(nodeRecord);
  }

  @Override
  public NodeRecordBO getRecordByNodeAndHistory(String historyId, String nodeId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getNodeId, nodeId));
    return convertNodeRecordBO(nodeRecord);
  }

  @Override
  public void updateRunningNodeStatus(String historyId, ProcessStatus processStatus) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(processStatus.getType());
    nodeRecord.setRecordResult(JSON.toJSONString(Collections.singleton(processStatus.getDesc())));
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getStatus, ProcessStatus.RUNNING.getType()));
  }

  private static NodeRecord convertNodeRecord(NodeRecordBO nodeRecordBO) {
    NodeRecord nodeRecord = OrikaUtil.convert(nodeRecordBO, NodeRecord.class);

    nodeRecord.setRecordResult(JSON.toJSONString(nodeRecordBO.getResult()));
    nodeRecord.setContext(JSON.toJSONString(nodeRecordBO.getPipelineContext()));
    return nodeRecord;
  }

  private static NodeRecordBO convertNodeRecordBO(NodeRecord nodeRecord) {
    NodeRecordBO nodeRecordBO = OrikaUtil.convert(nodeRecord, NodeRecordBO.class);
    Optional.ofNullable(nodeRecord.getRecordResult()).ifPresent(result ->
            nodeRecordBO.setResult(JSON.parseArray(result, String.class)));
    Optional.ofNullable(nodeRecord.getRecordResult()).ifPresent(result ->
            nodeRecordBO.setPipelineContext(JSON.parseObject(nodeRecord.getContext(),
                    new TypeReference<Map<String, Object>>() {
                    })));
    return nodeRecordBO;
  }
}
