package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.entity.po.feature.TaskInfo;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface ITaskRepository {

  IPage<TaskInfo>  getTaskList(String name, Integer pageNum, Integer size);

  boolean createTask(TaskInfoDto taskInfoDto);

  boolean updateTask(TaskInfoDto taskInfoDto);

  boolean deleteTask(String taskId);

  TaskInfoDto getTaskDetail(String taskId);

  List<TaskInfoDto> getAllTaskList(String serviceId);
}
