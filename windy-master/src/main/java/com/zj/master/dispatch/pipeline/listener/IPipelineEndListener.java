package com.zj.master.dispatch.pipeline.listener;

import com.zj.master.entity.vo.NodeStatusChange;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
public interface IPipelineEndListener {

  void handleEnd(NodeStatusChange nodeStatusChange);
}
