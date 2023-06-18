package com.zj.client.pipeline.executer.Invoker;


import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
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
   * @param recordId 任务Id
   * */
  boolean triggerRun(RequestContext requestContext, String recordId) throws Exception;

  /**
   * 查询节点任务执行状态
   * @param refreshContext  请求刷新状态的参数
   * @param recordId 任务Id
   * */
  String queryStatus(RefreshContext refreshContext, String recordId);
}
