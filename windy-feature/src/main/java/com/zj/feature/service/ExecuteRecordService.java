package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.dto.ExecuteRecordDTO;
import com.zj.feature.entity.po.ExecuteRecord;
import com.zj.feature.mapper.ExecuteRecordMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ExecuteRecordService extends ServiceImpl<ExecuteRecordMapper, ExecuteRecord> {

  public List<ExecuteRecordDTO> getExecuteRecords(String historyId) {
    List<ExecuteRecord> featureHistories = list(Wrappers.lambdaQuery(ExecuteRecord.class)
        .eq(ExecuteRecord::getHistoryId, historyId));

    if (CollectionUtils.isEmpty(featureHistories)) {
      return Collections.emptyList();
    }

    return featureHistories.stream().map(history -> {
      ExecuteRecordDTO historyDTO = new ExecuteRecordDTO();
      BeanUtils.copyProperties(history, historyDTO);
      historyDTO.setExecuteResult(JSON.parseArray(history.getExecuteResult()));
      return historyDTO;
    }).collect(Collectors.toList());
  }

  public boolean insert(ExecuteRecord executeRecord) {
    executeRecord.setExecuteRecordId(UUID.randomUUID().toString());
    executeRecord.setCreateTime(System.currentTimeMillis());
    return save(executeRecord);
  }

  public boolean deleteByHistoryId(String historyId) {
    return remove(
        Wrappers.lambdaQuery(ExecuteRecord.class).eq(ExecuteRecord::getHistoryId, historyId));
  }

  public boolean batchDeleteByHistoryId(List<String> historyIds) {
    if (CollectionUtils.isEmpty(historyIds)) {
      return true;
    }

    return remove(
        Wrappers.lambdaQuery(ExecuteRecord.class).in(ExecuteRecord::getHistoryId, historyIds));
  }
}
