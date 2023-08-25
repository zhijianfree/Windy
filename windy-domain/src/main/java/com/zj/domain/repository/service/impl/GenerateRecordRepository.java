package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.GenerateRecordDto;
import com.zj.domain.entity.po.service.GenerateRecord;
import com.zj.domain.mapper.service.GenerateRecordMapper;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GenerateRecordRepository extends
    ServiceImpl<GenerateRecordMapper, GenerateRecord> implements IGenerateRecordRepository {

  @Override
  public List<GenerateRecordDto> getServiceRecords(String serviceId) {
    List<GenerateRecord> records = list(
        Wrappers.lambdaQuery(GenerateRecord.class).eq(GenerateRecord::getServiceId, serviceId));
    return OrikaUtil.convertList(records, GenerateRecordDto.class);
  }

  @Override
  public boolean create(GenerateRecordDto generateRecordDto) {
    GenerateRecord generateRecord = OrikaUtil.convert(generateRecordDto, GenerateRecord.class);
    generateRecord.setCreateTime(System.currentTimeMillis());
    generateRecord.setUpdateTime(System.currentTimeMillis());
    return save(generateRecord);
  }

  @Override
  public boolean update(GenerateRecordDto generateRecordDto) {
    GenerateRecord generateRecord = OrikaUtil.convert(generateRecordDto, GenerateRecord.class);
    generateRecord.setUpdateTime(System.currentTimeMillis());
    return update(generateRecord, Wrappers.lambdaUpdate(GenerateRecord.class)
        .eq(GenerateRecord::getRecordId, generateRecord.getRecordId()));
  }
}
