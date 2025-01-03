package com.zj.client.handler.pipeline.executer.intercept;

import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 节点开始执行之后，需要轮询结果。
 * 方法{@link StatusQueryInterceptor#after(TaskNode, ProcessStatus)}将循环查询执行状态的任务添加到
 * {@link NodeStatusQueryLooper#addQueryTask(TaskNode)}
 *
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class StatusQueryInterceptor implements INodeExecuteInterceptor {

  private final NodeStatusQueryLooper nodeStatusQueryLooper;

  public StatusQueryInterceptor(NodeStatusQueryLooper nodeStatusQueryLooper) {
    this.nodeStatusQueryLooper = nodeStatusQueryLooper;
  }

  @Override
  public void before(TaskNode node) {

  }

  @Override
  public void after(TaskNode node, ProcessStatus status) {
    //只有正在执行的状态才需要轮询状态
    if (!Objects.equals(status.getType(), ProcessStatus.RUNNING.getType())) {
      return;
    }
    log.info("start run query recordId={} status={}", node.getRecordId(), status);
    nodeStatusQueryLooper.addQueryTask(node);
  }
}
