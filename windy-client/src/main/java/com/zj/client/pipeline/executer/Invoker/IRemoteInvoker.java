package com.zj.client.pipeline.executer.Invoker;


import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
public interface IRemoteInvoker {

  ExecuteType type();

  /**
   * 触发执行节点任务
   * @param requestContext
   * @param taskNode 任务
   * */
  boolean triggerRun(RequestContext requestContext, TaskNode taskNode) throws Exception;

  /**
   * 查询节点任务执行状态
   * @param refreshContext  请求刷新状态的参数
   * @param taskNode 任务详情
   * */
  String queryStatus(RefreshContext refreshContext, TaskNode taskNode);
}
