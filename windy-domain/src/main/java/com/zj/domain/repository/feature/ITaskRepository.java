package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.TaskInfoBO;
import com.zj.domain.entity.po.feature.TaskInfo;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface ITaskRepository {

  IPage<TaskInfo>  getTaskList(String name, Integer pageNum, Integer size);

  boolean createTask(TaskInfoBO taskInfoBO);

  boolean updateTask(TaskInfoBO taskInfoBO);

  boolean deleteTask(String taskId);

  TaskInfoBO getTaskDetail(String taskId);

  List<TaskInfoBO> getAllTaskList(String serviceId);
}
