package com.zj.pipeline.executer;


import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.executer.vo.TaskNode;

/**
 * @author guyuelan
 * @since 2022/5/30
 */
public interface INodeExecuteInterceptor {


  void before(TaskNode node);

  void after(TaskNode node, ProcessStatus status);
}
