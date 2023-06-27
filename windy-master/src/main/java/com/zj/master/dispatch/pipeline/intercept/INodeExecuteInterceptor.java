package com.zj.master.dispatch.pipeline.intercept;

import com.zj.master.entity.vo.TaskNode;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
public interface INodeExecuteInterceptor {

  void beforeExecute(TaskNode taskNode);
}
