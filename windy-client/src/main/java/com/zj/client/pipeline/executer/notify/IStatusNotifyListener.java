package com.zj.client.pipeline.executer.notify;


import com.zj.client.pipeline.executer.vo.PipelineStatusEvent;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
public interface IStatusNotifyListener {

  void statusChange(PipelineStatusEvent event);
}
