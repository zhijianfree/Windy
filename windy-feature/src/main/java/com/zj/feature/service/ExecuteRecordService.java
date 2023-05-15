package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.feature.entity.dto.ExecuteRecordDTO;
import com.zj.domain.entity.po.feature.ExecuteRecord;
import com.zj.domain.mapper.feeature.ExecuteRecordMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ExecuteRecordService extends ServiceImpl<ExecuteRecordMapper, ExecuteRecord> {

  @Autowired
  private UniqueIdService uniqueIdService;

  public List<ExecuteRecordDTO> getExecuteRecords(String historyId) {
    List<ExecuteRecord> featureHistories = list(Wrappers.lambdaQuery(ExecuteRecord.class)
        .eq(ExecuteRecord::getHistoryId, historyId));

    if (CollectionUtils.isEmpty(featureHistories)) {
      return Collections.emptyList();
    }

    return featureHistories.stream().map(history -> {
      ExecuteRecordDTO historyDTO = OrikaUtil.convert(history, ExecuteRecordDTO.class);
      historyDTO.setExecuteResult(JSON.parseArray(history.getExecuteResult()));
      return historyDTO;
    }).collect(Collectors.toList());
  }

  public boolean insert(ExecuteRecord executeRecord) {
    executeRecord.setExecuteRecordId(uniqueIdService.getUniqueId());
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
