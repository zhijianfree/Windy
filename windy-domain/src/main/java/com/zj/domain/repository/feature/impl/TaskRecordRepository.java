package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.po.feature.TaskRecord;
import com.zj.domain.mapper.feeature.TaskRecordMapper;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class TaskRecordRepository extends ServiceImpl<TaskRecordMapper, TaskRecord> implements
    ITaskRecordRepository {

  @Override
  public boolean save(TaskRecordDto taskRecordDto) {
    TaskRecord taskRecord = OrikaUtil.convert(taskRecordDto, TaskRecord.class);
    long dateNow = System.currentTimeMillis();
    taskRecord.setCreateTime(dateNow);
    taskRecord.setUpdateTime(dateNow);
    return save(taskRecord);
  }

  @Override
  public boolean updateRecordStatus(String recordId, int status) {
    TaskRecord taskRecord = new TaskRecord();
    taskRecord.setRecordId(recordId);
    taskRecord.setStatus(status);
    taskRecord.setUpdateTime(System.currentTimeMillis());
    return saveOrUpdate(taskRecord,
        Wrappers.lambdaUpdate(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  @Override
  public IPage<TaskRecordDto> getTaskRecordPage(Integer pageNum, Integer size) {
    IPage<TaskRecord> page = new Page<>(pageNum, size);
    IPage<TaskRecord> recordIPage = page(page,
        Wrappers.lambdaQuery(TaskRecord.class).orderByDesc(TaskRecord::getUpdateTime));

    List<TaskRecordDto> list = OrikaUtil.convertList(recordIPage.getRecords(),
        TaskRecordDto.class);
    Page<TaskRecordDto> recordDtoPage = new Page<>();
    recordDtoPage.setTotal(recordDtoPage.getTotal());
    recordDtoPage.setRecords(list);
    return recordDtoPage;
  }

  @Override
  public TaskRecordDto getTaskRecord(String recordId) {
    TaskRecord taskRecord = getOne(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
    return OrikaUtil.convert(taskRecord, TaskRecordDto.class);
  }

  @Override
  public boolean deleteTaskRecord(String recordId) {
    return remove(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  @Override
  public List<TaskRecordDto> getTaskRecordsOrderByTime(String taskId) {
    List<TaskRecord> taskRecords = list(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getTaskId, taskId)
            .orderByDesc(TaskRecord::getCreateTime));
    return OrikaUtil.convertList(taskRecords, TaskRecordDto.class);
  }
}
