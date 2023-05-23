package com.zj.master.dispatch;

import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.master.entity.dto.TaskDetailDto;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IDispatchExecutor {

  Integer type();

  String dispatch(TaskDetailDto task);

  boolean resume(DispatchLogDto taskLog);
}
