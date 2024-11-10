package com.zj.domain.repository.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.generate.GenerateDetail;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.po.service.GenerateRecord;
import com.zj.domain.mapper.service.GenerateRecordMapper;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GenerateRecordRepository extends ServiceImpl<GenerateRecordMapper, GenerateRecord> implements IGenerateRecordRepository {

    @Override
    public List<GenerateRecordBO> getServiceRecords(String serviceId) {
        List<GenerateRecord> records =
                list(Wrappers.lambdaQuery(GenerateRecord.class).eq(GenerateRecord::getServiceId, serviceId));
        return OrikaUtil.convertList(records, GenerateRecordBO.class);
    }

    @Override
    public List<GenerateRecordBO> getGenerateRecord(String serviceId, String version) {
        List<GenerateRecord> generateRecords =
                list(Wrappers.lambdaQuery(GenerateRecord.class).eq(GenerateRecord::getServiceId, serviceId)
                        .eq(GenerateRecord::getVersion, version));
        return generateRecords.stream().map(GenerateRecordRepository::convertGenerateRecordBO).collect(Collectors.toList());
    }

    @Override
    public boolean create(GenerateRecordBO generateRecordBO) {
        GenerateRecord generateRecord = convertGenerateRecord(generateRecordBO);
        generateRecord.setCreateTime(System.currentTimeMillis());
        generateRecord.setUpdateTime(System.currentTimeMillis());
        return save(generateRecord);
    }

    @Override
    public boolean update(GenerateRecordBO generateRecordBO) {
        GenerateRecord generateRecord = convertGenerateRecord(generateRecordBO);
        generateRecord.setUpdateTime(System.currentTimeMillis());
        return update(generateRecord, Wrappers.lambdaUpdate(GenerateRecord.class).eq(GenerateRecord::getRecordId,
                generateRecord.getRecordId()));
    }

    private static GenerateRecord convertGenerateRecord(GenerateRecordBO generateRecordBO) {
        GenerateRecord generateRecord = OrikaUtil.convert(generateRecordBO, GenerateRecord.class);
        generateRecord.setExecuteParams(JSON.toJSONString(generateRecordBO.getGenerateParams()));
        generateRecord.setResult(JSON.toJSONString(generateRecordBO.getGenerateResult()));
        return generateRecord;
    }

    private static GenerateRecordBO convertGenerateRecordBO(GenerateRecord generateRecord) {
        GenerateRecordBO generateRecordBO = OrikaUtil.convert(generateRecord, GenerateRecordBO.class);
        generateRecordBO.setGenerateResult(JSON.parseArray(generateRecord.getResult(), String.class));
        generateRecordBO.setGenerateParams(JSON.parseObject(generateRecord.getExecuteParams(), GenerateDetail.class));
        return generateRecordBO;
    }
}
