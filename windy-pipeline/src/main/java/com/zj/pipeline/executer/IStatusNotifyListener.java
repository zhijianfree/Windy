package com.zj.pipeline.executer;

import com.zj.pipeline.executer.vo.PipelineStatusEvent;

/**
 * @author falcon
 * @since 2023/3/30
 */
public interface IStatusNotifyListener {

  void statusChange(PipelineStatusEvent event);
}