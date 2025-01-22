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

  /**
   * 模糊查询任务列表
   *
   * @param name      任务名称
   * @param pageNum   页码
   * @param size      每页数量
   * @param userId    用户ID
   * @param serviceId 服务ID
   * @return 任务列表
   */
  IPage<TaskInfo> getFuzzTaskList(String name, Integer pageNum, Integer size, String userId, String serviceId);

  /**
   * 创建任务
   * @param taskInfoBO 任务信息
   * @return 是否成功
   */
  boolean createTask(TaskInfoBO taskInfoBO);

  /**
   * 更新任务
   * @param taskInfoBO 任务信息
   * @return 是否成功
   */
  boolean updateTask(TaskInfoBO taskInfoBO);

  /**
   * 删除任务
   * @param taskId 任务ID
   * @return 是否成功
   */
  boolean deleteTask(String taskId);

  /**
   * 获取任务详情
   * @param taskId 任务ID
   * @return 任务详情
   */
  TaskInfoBO getTaskDetail(String taskId);

  /**
   * 获取服务所有任务列表
   * @param serviceId 服务ID
   * @return 任务列表
   */
  List<TaskInfoBO> getAllTaskList(String serviceId);
}
