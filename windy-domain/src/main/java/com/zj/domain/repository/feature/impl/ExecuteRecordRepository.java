package com.zj.domain.repository.feature.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.entity.po.feature.ExecuteRecord;
import com.zj.domain.mapper.feeature.ExecuteRecordMapper;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Repository
public class ExecuteRecordRepository extends
    ServiceImpl<ExecuteRecordMapper, ExecuteRecord> implements IExecuteRecordRepository {

  @Override
  public List<ExecuteRecordBO> getExecuteRecords(String historyId) {
    List<ExecuteRecord> executeRecords = list(Wrappers.lambdaQuery(ExecuteRecord.class)
        .eq(ExecuteRecord::getHistoryId, historyId));
    if (CollectionUtils.isEmpty(executeRecords)) {
      return Collections.emptyList();
    }
    return executeRecords.stream().map(ExecuteRecordRepository::convertExecuteRecordBO).collect(Collectors.toList());
  }

  @Override
  public boolean saveRecord(ExecuteRecordBO executeRecordBO) {
    ExecuteRecord executeRecord = convertExecuteRecord(executeRecordBO);
    executeRecord.setCreateTime(System.currentTimeMillis());
    executeRecord.setUpdateTime(System.currentTimeMillis());
    return save(executeRecord);
  }

  @Override
  public boolean updateStatusAndResult(ExecuteRecordBO executeRecordBO) {
    ExecuteRecord executeRecord = new ExecuteRecord();
    executeRecord.setExecuteRecordId(executeRecordBO.getExecuteRecordId());
    executeRecord.setStatus(executeRecordBO.getStatus());
    executeRecord.setExecuteResult(JSON.toJSONString(executeRecordBO.getExecuteResult()));
    executeRecord.setUpdateTime(System.currentTimeMillis());
    return update(executeRecord, Wrappers.lambdaUpdate(ExecuteRecord.class).eq(ExecuteRecord::getExecuteRecordId,
            executeRecordBO.getExecuteRecordId()));
  }

  @Override
  public boolean deleteByHistoryId(String historyId) {
    return remove(Wrappers.lambdaQuery(ExecuteRecord.class).eq(ExecuteRecord::getHistoryId, historyId));
  }

  @Override
  public boolean batchDeleteByHistoryId(List<String> historyIds) {
    if (CollectionUtils.isEmpty(historyIds)) {
      return false;
    }
    return remove(Wrappers.lambdaQuery(ExecuteRecord.class).in(ExecuteRecord::getHistoryId, historyIds));
  }

  private static ExecuteRecordBO convertExecuteRecordBO(ExecuteRecord executeRecord) {
    ExecuteRecordBO executeRecordBO = OrikaUtil.convert(executeRecord, ExecuteRecordBO.class);
    executeRecordBO.setExecuteResult(JSON.parseArray(executeRecord.getExecuteResult(), FeatureResponse.class));
    return executeRecordBO;
  }

  private static ExecuteRecord convertExecuteRecord(ExecuteRecordBO executeRecordBO) {
    ExecuteRecord executeRecord = OrikaUtil.convert(executeRecordBO, ExecuteRecord.class);
    executeRecord.setExecuteResult(JSON.toJSONString(executeRecordBO.getExecuteResult()));
    return executeRecord;
  }
}
