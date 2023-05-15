package com.zj.master.dispatch;

import com.zj.master.entity.dto.TaskDetailDto;

/**
 * @author falcon
 * @since 2023/5/15
 */
public interface IDispatchExecutor {

  Integer type();

  boolean dispatch(TaskDetailDto task);
}
