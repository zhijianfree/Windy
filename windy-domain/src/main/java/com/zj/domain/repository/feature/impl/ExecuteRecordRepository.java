package com.zj.domain.repository.feature.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.entity.po.feature.ExecuteRecord;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.mapper.feeature.ExecuteRecordMapper;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

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
      com.zj.domain.entity.dto.feature.ExecuteRecordDto historyDTO = OrikaUtil.convert(history, com.zj.domain.entity.dto.feature.ExecuteRecordDto.class);
      historyDTO.setExecuteResult(JSON.parseArray(history.getExecuteResult()));
      return historyDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public boolean saveRecord(ExecuteRecordDto executeRecord) {
    ExecuteRecord record = OrikaUtil.convert(executeRecord, ExecuteRecord.class);
    record.setUpdateTime(System.currentTimeMillis());
    return save(record);
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
