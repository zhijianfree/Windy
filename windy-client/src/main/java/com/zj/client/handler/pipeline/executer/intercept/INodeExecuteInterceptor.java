package com.zj.client.handler.pipeline.executer.intercept;


import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;

/**
 * @author guyuelan
 * @since 2022/5/30
 */
public interface INodeExecuteInterceptor {

  /**
   * 节点运行之前执行
   * */
  void before(TaskNode node);

  /**
   * 节点运行之后执行
   * */
  void after(TaskNode node, ProcessStatus status);
}
