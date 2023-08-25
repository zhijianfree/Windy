package com.zj.master.dispatch;

import com.zj.common.enums.LogType;
import com.zj.common.model.DispatchTaskModel;
import com.zj.domain.entity.dto.log.DispatchLogDto;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IDispatchExecutor {

  LogType type();

  /**
   * 传入的任务是否在当前实例中
   * */
  boolean isExistInJvm(DispatchLogDto taskLog);

  /**
   * 分配任务给子节点
   * @param task 任务内容
   * @param logId 分发任务的记录Id
   * */
  Object dispatch(DispatchTaskModel task, String logId);

  /**
   * 恢复执行任务
   * */
  boolean resume(DispatchLogDto taskLog);

  /**
   * 当前待执行个数，监控统计使用
   * */
  Integer getExecuteCount();
}
