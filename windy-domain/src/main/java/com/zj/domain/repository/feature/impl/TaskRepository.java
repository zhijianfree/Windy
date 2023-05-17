package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.entity.po.feature.TaskInfo;
import com.zj.domain.mapper.feeature.TaskInfoMapper;
import com.zj.domain.repository.feature.ITaskRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Repository
public class TaskRepository extends ServiceImpl<TaskInfoMapper, TaskInfo> implements
    ITaskRepository {

  @Override
  public IPage<TaskInfo> getTaskList(String name, Integer pageNum, Integer size) {
    LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery(TaskInfo.class);
    if (StringUtils.isNotBlank(name)) {
      wrapper = wrapper.like(TaskInfo::getTaskName, name);
    }
    IPage<TaskInfo> page = new Page<>(pageNum, size);
    return page(page, wrapper);
  }

  @Override
  public boolean createTask(TaskInfoDto taskInfoDto) {
    TaskInfo taskInfo = OrikaUtil.convert(taskInfoDto, TaskInfo.class);
    taskInfo.setCreateTime(System.currentTimeMillis());
    taskInfo.setUpdateTime(System.currentTimeMillis());
    return save(taskInfo);
  }

  @Override
  public boolean updateTask(TaskInfoDto taskInfoDto) {
    TaskInfo taskInfo = OrikaUtil.convert(taskInfoDto, TaskInfo.class);
    taskInfo.setUpdateTime(System.currentTimeMillis());

    return update(taskInfo,
        Wrappers.lambdaUpdate(TaskInfo.class).eq(TaskInfo::getTaskId, taskInfoDto.getTaskId()));
  }

  @Override
  public boolean deleteTask(String taskId) {
    return remove(Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
  }

  @Override
  public TaskInfoDto getTaskDetail(String taskId) {
    TaskInfo taskInfo = getOne(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
    return OrikaUtil.convert(taskInfo, TaskInfoDto.class);
  }

  @Override
  public List<TaskInfoDto> getAllTaskList(String serviceId) {
    List<TaskInfo> taskInfos = list(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getServiceId, serviceId));
    return taskInfos.stream().map(task -> OrikaUtil.convert(task, TaskInfoDto.class))
        .collect(Collectors.toList());
  }
}
