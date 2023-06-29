package com.zj.master.dispatch.pipeline.intercept;

import com.zj.master.entity.vo.TaskNode;

/**
 * 每个子节点运行之前的拦截器
 *
 * @author guyuelan
 * @since 2023/6/15
 */
public interface INodeExecuteInterceptor {

  void beforeExecute(TaskNode taskNode);
}
