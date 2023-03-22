package com.zj.pipeline.executer.handler;


import com.zj.pipeline.executer.vo.RequestContext;

/**
 * @author falcon
 * @since 2022/5/25
 */
public interface IRemoteInvoker {

  String type();

  boolean execute(RequestContext requestContext, String taskId);
}
