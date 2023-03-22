package com.zj.pipeline.executer;


import com.zj.pipeline.executer.vo.TaskNode;

/**
 * @author falcon
 * @since 2022/5/30
 */
public interface IPipelineInterceptor {


  void before(TaskNode node);

  void after(TaskNode node);
}
