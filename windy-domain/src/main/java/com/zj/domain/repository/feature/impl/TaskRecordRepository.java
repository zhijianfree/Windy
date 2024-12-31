package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.po.feature.TaskRecord;
import com.zj.domain.mapper.feeature.TaskRecordMapper;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class TaskRecordRepository extends ServiceImpl<TaskRecordMapper, TaskRecord> implements
    ITaskRecordRepository {

  @Override
  public boolean save(TaskRecordBO taskRecordBO) {
    TaskRecord taskRecord = OrikaUtil.convert(taskRecordBO, TaskRecord.class);
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
    return saveOrUpdate(taskRecord, Wrappers.lambdaUpdate(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  @Override
  public boolean updateRecordStatusAndPercent(String recordId, Integer status, int percent) {
    TaskRecord taskRecord = new TaskRecord();
    taskRecord.setRecordId(recordId);
    taskRecord.setStatus(status);
    taskRecord.setPercent(percent);
    taskRecord.setUpdateTime(System.currentTimeMillis());
    return saveOrUpdate(taskRecord, Wrappers.lambdaUpdate(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  @Override
  public PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size) {
    IPage<TaskRecord> page = new Page<>(pageNum, size);
    IPage<TaskRecord> recordIPage = page(page,
        Wrappers.lambdaQuery(TaskRecord.class).orderByDesc(TaskRecord::getUpdateTime));
    return convertRecordPage(recordIPage);
  }

  @Override
  public TaskRecordBO getTaskRecord(String recordId) {
    TaskRecord taskRecord = getOne(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
    return OrikaUtil.convert(taskRecord, TaskRecordBO.class);
  }

  @Override
  public boolean deleteTaskRecord(String recordId) {
    return remove(Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getRecordId, recordId));
  }

  @Override
  public List<TaskRecordBO> getTaskRecords(String taskId) {
    List<TaskRecord> taskRecords = list(
        Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getTaskId, taskId)
            .orderByDesc(TaskRecord::getCreateTime));
    return OrikaUtil.convertList(taskRecords, TaskRecordBO.class);
  }

  @Override
  public List<TaskRecordBO> getOldTaskRecord(long oldTime) {
    List<TaskRecord> taskRecordList = list(Wrappers.lambdaQuery(TaskRecord.class).le(TaskRecord::getCreateTime, oldTime));
    return OrikaUtil.convertList(taskRecordList, TaskRecordBO.class);
  }

  @Override
  public TaskRecordBO getTaskRecordByTrigger(String triggerId) {
    TaskRecord taskRecord = getOne(
            Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getTriggerId, triggerId));
    return OrikaUtil.convert(taskRecord, TaskRecordBO.class);
  }

  @Override
  public PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer pageNum, Integer size) {
    IPage<TaskRecord> page = new Page<>(pageNum, size);
    IPage<TaskRecord> recordIPage = page(page,
            Wrappers.lambdaQuery(TaskRecord.class).eq(TaskRecord::getTriggerId, triggerId).orderByDesc(TaskRecord::getCreateTime));
      return convertRecordPage(recordIPage);
  }

  private static PageSize<TaskRecordBO> convertRecordPage(IPage<TaskRecord> recordIPage) {
    List<TaskRecordBO> list = OrikaUtil.convertList(recordIPage.getRecords(), TaskRecordBO.class);
    list = list.stream().peek(taskRecord -> {
      String desc = ProcessStatus.exchange(taskRecord.getStatus()).getDesc();
      taskRecord.setStatusName(desc);
    }).collect(Collectors.toList());

    PageSize<TaskRecordBO> recordDtoPage = new PageSize<>();
    recordDtoPage.setTotal(recordIPage.getTotal());
    recordDtoPage.setData(list);
    return recordDtoPage;
  }
}
