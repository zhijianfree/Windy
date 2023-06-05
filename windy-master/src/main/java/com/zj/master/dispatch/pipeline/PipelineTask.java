package com.zj.master.dispatch.pipeline;

import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 标记队列用来将同一个流水线的任务打标并放到一起
 * 这样就可以根据流水线的顺序执行
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
public class PipelineTask {

  /**
   * 流水线历史记录Id
   * */
  private String historyId;

  /**
   * 流线Id
   * */
  private String pipelineId;

  /**
   * master节点执行记录的Id，恢复奔溃任务时使用
   * */
  private String logId;

  private LinkedBlockingQueue<TaskNode> taskNodes;

  public void addAll(List<TaskNode> taskNodes){
    if (CollectionUtils.isEmpty(this.taskNodes)){
      this.taskNodes = new LinkedBlockingQueue<>();
    }

    this.taskNodes.addAll(taskNodes);
  }
}
