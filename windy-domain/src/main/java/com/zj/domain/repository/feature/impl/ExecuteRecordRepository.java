package com.zj.domain.repository.feature.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
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
  public List<ExecuteRecordDto> getExecuteRecords(String historyId) {
    List<ExecuteRecord> featureHistories = list(Wrappers.lambdaQuery(ExecuteRecord.class)
        .eq(ExecuteRecord::getHistoryId, historyId));

    if (CollectionUtils.isEmpty(featureHistories)) {
      return Collections.emptyList();
    }

    return featureHistories.stream().map(history -> {
      ExecuteRecordDto historyDTO = OrikaUtil.convert(history, ExecuteRecordDto.class);
      historyDTO.setExecuteResult(JSON.parseArray(history.getExecuteResult()));
      return historyDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public boolean saveRecord(ExecuteRecordDto executeRecordDto) {
    ExecuteRecord executeRecord = OrikaUtil.convert(executeRecordDto, ExecuteRecord.class);
    executeRecord.setUpdateTime(System.currentTimeMillis());
    return save(executeRecord);
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
}
