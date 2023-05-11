package com.zj.pipeline.executer;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.vo.PipelineTask;
import com.zj.pipeline.executer.vo.ExecuteParam;
import com.zj.pipeline.executer.vo.PipelineRecord;
import com.zj.pipeline.executer.vo.Stage;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.NodeRecordService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流水线的执行支持按照每个阶段并行串行执行，执行过程中任意阶段执行失败，就表示整个流水线执行失败。\n 流水线包含必要的
 * 开始和结束节点，中间则可以任意添加节点任务。每个任务节点支持添加任意类型的执行参数， 比如创建时指定环境，比如运行指定 运行上限等。流水线可以添加等待节点（可以是执行等待或者是审批等待）。
 *
 * @author guyuelan
 * @since 2022/5/23
 */
@Slf4j
@Component
public class PipelineExecutor {

  private final ExecuteProxy executeProxy;

  private final NodeRecordService nodeRecordService;

  private final UniqueIdService uniqueIdService;

  public PipelineExecutor(ExecuteProxy executeProxy, NodeRecordService nodeRecordService,
      UniqueIdService uniqueIdService) {
    this.executeProxy = executeProxy;
    this.nodeRecordService = nodeRecordService;
    this.uniqueIdService = uniqueIdService;
  }

  public String execute(ExecuteParam executeParam) {
    List<Stage> stages = executeParam.getStages();
    /*
     * 每执行一次任务，应该都有执行任务的记录。任务记录的最小单位是节点任务，所有任务的状态都是异步刷新。任务状态异步刷新
     * 那么则需要每个任务在添加时`，都明确配置查询状态的接口。
     * */
    log.info("start run pipeline={} name={}", executeParam.getPipelineId(), executeParam.getName());
    String historyId = uniqueIdService.getUniqueId();
    PipelineRecord pipelineRecord = PipelineRecord.builder()
        .pipelineId(executeParam.getPipelineId()).historyId(historyId)
        .pipelineStatus(ProcessStatus.RUNNING.getType()).build();
    nodeRecordService.savePipelineHistory(pipelineRecord);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(executeParam.getPipelineId());
    pipelineTask.setHistoryId(historyId);
    stages.forEach(stage -> {
      List<TaskNode> taskNodes = stage.getNodeList().stream()
          .peek(node -> node.setHistoryId(historyId))
          .collect(Collectors.toList());
      pipelineTask.addAll(taskNodes);
    });
    executeProxy.execute(pipelineTask);
    return historyId;
  }
}
