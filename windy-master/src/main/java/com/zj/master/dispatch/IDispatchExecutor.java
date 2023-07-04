package com.zj.master.dispatch;

import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.master.entity.dto.TaskDetailDto;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IDispatchExecutor {

  Integer type();

  /**
   * 传入的任务是否在当前实例中
   * */
  boolean isExitInJvm(DispatchLogDto taskLog);

  /**
   * 分配任务给子节点
   * */
  String dispatch(TaskDetailDto task);

  /**
   * 恢复执行任务
   * */
  boolean resume(DispatchLogDto taskLog);

  /**
   * 当前待执行个数，监控统计使用
   * */
  Integer getExecuteCount();
}
