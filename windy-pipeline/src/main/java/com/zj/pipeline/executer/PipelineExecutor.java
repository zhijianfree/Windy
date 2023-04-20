package com.zj.pipeline.executer;

import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.vo.PipelineRecord;
import com.zj.pipeline.executer.vo.ExecuteParam;
import com.zj.pipeline.executer.vo.Stage;
import com.zj.pipeline.service.PipelineNodeRecordService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 流水线的执行支持按照每个阶段并行串行执行，执行过程中任意阶段执行失败，就表示整个流水线执行失败。\n 流水线包含必要的
 * 开始和结束节点，中间则可以任意添加节点任务。每个任务节点支持添加任意类型的执行参数， 比如创建时指定环境，比如运行指定 运行上限等。流水线可以添加等待节点（可以是执行等待或者是审批等待）。
 *
 * @author falcon
 * @since 2022/5/23
 */
@Slf4j
@Component
public class PipelineExecutor {

  private NodeExecutor nodeExecutor;

  private Executor executor = new ThreadPoolExecutor(5, 10, 3, TimeUnit.HOURS,
      new LinkedBlockingQueue<>(100), new CallerRunsPolicy());

  private final PipelineNodeRecordService pipelineNodeRecordService;

  public PipelineExecutor(NodeExecutor nodeExecutor, PipelineNodeRecordService pipelineNodeRecordService) {
    this.nodeExecutor = nodeExecutor;
    this.pipelineNodeRecordService = pipelineNodeRecordService;

  }

  public void execute(ExecuteParam executeParam) {
    List<Stage> stages = executeParam.getStages();
    if (CollectionUtils.isEmpty(stages)) {
      log.info("stage list is empty");
      throw new IllegalArgumentException("stages is empty");
    }

    /*
     * 每执行一次任务，应该都有执行任务的记录。任务记录的最小单位是节点任务，所有任务的状态都是异步刷新。任务状态异步刷新
     * 那么则需要每个任务在添加时`，都明确配置查询状态的接口。
     * */
    log.info("start run pipeline={} name={}", executeParam.getPipelineId(), executeParam.getName());
    String historyId = UUID.randomUUID().toString();
    PipelineRecord pipelineRecord = PipelineRecord.builder()
        .pipelineId(executeParam.getPipelineId()).historyId(historyId)
        .pipelineStatus(ProcessStatus.RUNNING.getType()).build();
    pipelineNodeRecordService.savePipelineHistory(pipelineRecord);

    //todo 并行和串行执行
    stages.forEach(stage -> {
      log.info("start run stage={} name={}", stage.getStageId(), stage.getStageName());
      List<CompletableFuture<Void>> futures = stage.getNodeList().stream().map(
          node -> CompletableFuture.runAsync(() -> nodeExecutor.runNodeTask(historyId, node),
              executor)).collect(Collectors.toList());

      log.info("run after nodes");
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}))
          .whenComplete((unused, throwable) -> {
            log.info("pipeline run complete pipelineId={}", executeParam.getPipelineId());
            if (Objects.nonNull(throwable)) {
              log.info("run error", throwable);
            }
          });
    });
  }
}
