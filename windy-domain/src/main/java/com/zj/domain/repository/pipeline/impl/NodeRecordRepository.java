package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.mapper.pipeline.NodeRecordMapper;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Slf4j
@Repository
public class NodeRecordRepository extends ServiceImpl<NodeRecordMapper, NodeRecord> implements
    INodeRecordRepository {

  @Override
  public boolean saveNodeRecord(NodeRecord nodeRecord) {
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
    nodeRecord.setCreateTime(dateNow);
    return save(nodeRecord);
  }

  @Override
  public boolean updateNodeRecord(NodeRecord nodeRecord) {
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
    return update(nodeRecord, Wrappers.lambdaUpdate(NodeRecord.class)
        .eq(NodeRecord::getRecordId, nodeRecord.getRecordId()));
  }

  @Override
  public boolean updateNodeRecordStatus(String recordId, Integer type, String message) {
    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setStatus(type);
    nodeRecord.setRecordId(recordId);
    nodeRecord.setResult(message);
    long dateNow = System.currentTimeMillis();
    nodeRecord.setUpdateTime(dateNow);
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
  public List<NodeRecord> getRecordsByHistoryId(String historyId) {
    return list(Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId));
  }

  @Override
  public NodeRecord getRecordById(String recordId) {
    return getOne(Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getRecordId, recordId));
  }
}
