package com.zj.client.handler.pipeline.executer.notify;


import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
public interface IPipelineStatusListener {

  void statusChange(PipelineStatusEvent event);
}
