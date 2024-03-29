package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.mapper.pipeline.NodeRecordMapper;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import java.util.List;
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
  public boolean saveNodeRecord(NodeRecordDto nodeRecordDto) {
    NodeRecord nodeRecord = OrikaUtil.convert(nodeRecordDto, NodeRecord.class);
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
    nodeRecord.setCreateTime(dateNow);
    return save(nodeRecord);
  }

  @Override
  public boolean updateNodeRecord(NodeRecordDto nodeRecordDto) {
    NodeRecord nodeRecord = OrikaUtil.convert(nodeRecordDto, NodeRecord.class);
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
    return update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getRecordId, nodeRecord.getRecordId()));
  }

  @Override
  public boolean updateNodeRecordStatus(String recordId, Integer type, String message) {
    NodeRecordDto nodeRecord = new NodeRecordDto();
    nodeRecord.setStatus(type);
    nodeRecord.setRecordId(recordId);
    nodeRecord.setResult(message);
    return updateNodeRecord(nodeRecord);
  }

  @Override
  public boolean batchUpdateStatus(List<String> recordIds, ProcessStatus processStatus) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(processStatus.getType());
    nodeRecord.setResult(JSON.toJSONString(Collections.singleton(processStatus.getDesc())));
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    boolean batchUpdate = update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).in(NodeRecord::getRecordId, recordIds));
    log.info("batch update record status={}", batchUpdate);
    return batchUpdate;
  }

  @Override
  public List<NodeRecordDto> getRecordsByHistoryId(String historyId) {
    List<NodeRecord> nodeRecords = list(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId));
    return OrikaUtil.convertList(nodeRecords, NodeRecordDto.class);
  }

  @Override
  public NodeRecordDto getRecordById(String recordId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
    return OrikaUtil.convert(nodeRecord, NodeRecordDto.class);
  }

  @Override
  public NodeRecordDto getRecordByNodeAndHistory(String historyId, String nodeId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getNodeId, nodeId));
    return OrikaUtil.convert(nodeRecord, NodeRecordDto.class);
  }

  @Override
  public void updateRunningNodeStatus(String historyId, ProcessStatus processStatus) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(processStatus.getType());
    nodeRecord.setResult(JSON.toJSONString(Collections.singleton(processStatus.getDesc())));
    nodeRecord.setUpdateTime(System.currentTimeMillis());
    update(nodeRecord,
        Wrappers.lambdaUpdate(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getStatus, ProcessStatus.RUNNING.getType()));
  }
}
