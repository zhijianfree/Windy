package com.zj.pipeline.executer.intercept;

import com.zj.pipeline.executer.INodeExecuteInterceptor;
import com.zj.pipeline.executer.NodeStatusQueryLooper;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.vo.NodeConfig;
import com.zj.pipeline.executer.vo.TaskNode;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Component
public class StatusQueryInterceptor implements INodeExecuteInterceptor {

  @Autowired
  private NodeStatusQueryLooper nodeStatusQueryLooper;

  @Override
  public void before(TaskNode node) {

  }

  @Override
  public void after(TaskNode node, Integer status) {
    NodeConfig nodeConfig = node.getNodeConfig();
    if (!Objects.equals(status, ProcessStatus.FAIL.getType()) && !nodeConfig.isIgnoreError()) {
      nodeStatusQueryLooper.addQuestTask(node);
    }
  }
}
