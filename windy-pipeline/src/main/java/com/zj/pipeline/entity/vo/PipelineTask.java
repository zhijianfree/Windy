package com.zj.pipeline.entity.vo;

import com.zj.pipeline.executer.vo.TaskNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 标记队列用来将同一个流水线的任务打标并放到一起
 * 这样就可以根据流水线的顺序执行
 * @author falcon
 * @since 2023/5/9
 */
@Data
public class PipelineTask {

  private String historyId;

  private String pipelineId;

  private LinkedBlockingQueue<TaskNode> taskNodes;

  public void addAll(List<TaskNode> taskNodes){
    if (CollectionUtils.isEmpty(this.taskNodes)){
      this.taskNodes = new LinkedBlockingQueue<>();
    }

    this.taskNodes.addAll(taskNodes);
  }
}
