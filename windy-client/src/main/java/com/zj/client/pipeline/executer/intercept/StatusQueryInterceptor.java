package com.zj.client.pipeline.executer.intercept;

import com.zj.client.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 节点开始执行之后，需要轮询结果。
 * 方法{@link StatusQueryInterceptor#after(TaskNode, ProcessStatus)}中将查询任务添加到
 * {@link NodeStatusQueryLooper#addQuestTask(TaskNode)}
 *
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class StatusQueryInterceptor implements INodeExecuteInterceptor {

  @Autowired
  private NodeStatusQueryLooper nodeStatusQueryLooper;

  @Override
  public void before(TaskNode node) {

  }

  @Override
  public void after(TaskNode node, ProcessStatus status) {
    log.info("start run query recordId={} status={}", node.getRecordId(), status);
    //只有执行触发任务成功才需要轮询状态
    if (!status.isFailStatus()) {
      nodeStatusQueryLooper.addQuestTask(node);
    }
  }
}
