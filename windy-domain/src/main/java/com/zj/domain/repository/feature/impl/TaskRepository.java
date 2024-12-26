package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.domain.entity.po.feature.TaskInfo;
import com.zj.domain.mapper.feeature.TaskInfoMapper;
import com.zj.domain.repository.feature.ITaskRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Repository
public class TaskRepository extends ServiceImpl<TaskInfoMapper, TaskInfo> implements
    ITaskRepository {

  @Override
  public IPage<TaskInfo> getFuzzTaskList(String name, Integer pageNum, Integer size) {
    LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery(TaskInfo.class);
    if (StringUtils.isNotBlank(name)) {
      wrapper = wrapper.like(TaskInfo::getTaskName, name);
    }
    IPage<TaskInfo> page = new Page<>(pageNum, size);
    return page(page, wrapper);
  }

  @Override
  public boolean createTask(TaskInfoBO taskInfoBO) {
    TaskInfo taskInfo = OrikaUtil.convert(taskInfoBO, TaskInfo.class);
    taskInfo.setCreateTime(System.currentTimeMillis());
    taskInfo.setUpdateTime(System.currentTimeMillis());
    return save(taskInfo);
  }

  @Override
  public boolean updateTask(TaskInfoBO taskInfoBO) {
    TaskInfo taskInfo = OrikaUtil.convert(taskInfoBO, TaskInfo.class);
    taskInfo.setUpdateTime(System.currentTimeMillis());

    return update(taskInfo,
        Wrappers.lambdaUpdate(TaskInfo.class).eq(TaskInfo::getTaskId, taskInfoBO.getTaskId()));
  }

  @Override
  public boolean deleteTask(String taskId) {
    return remove(Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
  }

  @Override
  public TaskInfoBO getTaskDetail(String taskId) {
    TaskInfo taskInfo = getOne(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getTaskId, taskId));
    return OrikaUtil.convert(taskInfo, TaskInfoBO.class);
  }

  @Override
  public List<TaskInfoBO> getAllTaskList(String serviceId) {
    List<TaskInfo> taskInfos = list(
        Wrappers.lambdaQuery(TaskInfo.class).eq(TaskInfo::getServiceId, serviceId));
    return taskInfos.stream().map(task -> OrikaUtil.convert(task, TaskInfoBO.class))
        .collect(Collectors.toList());
  }
}
