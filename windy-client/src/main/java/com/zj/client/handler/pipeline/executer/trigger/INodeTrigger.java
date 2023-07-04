package com.zj.client.handler.pipeline.executer.trigger;


import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ExecuteType;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
public interface INodeTrigger {

  ExecuteType type();

  /**
   * 触发执行节点任务
   * @param triggerContext 执行上下文参数
   * @param taskNode 任务
   * */
  void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws Exception;

  /**
   * 查询节点任务执行状态
   * @param refreshContext  请求刷新状态的参数
   * @param taskNode 任务详情
   * */
  String queryStatus(RefreshContext refreshContext, TaskNode taskNode);
}
