package com.zj.client.pipeline.executer.intercept;


import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;

/**
 * @author guyuelan
 * @since 2022/5/30
 */
public interface INodeExecuteInterceptor {


  void before(TaskNode node);

  void after(TaskNode node, ProcessStatus status);
}
