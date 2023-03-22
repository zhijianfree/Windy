package com.zj.pipeline.executer;

import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.handler.IRemoteInvoker;
import com.zj.pipeline.executer.handler.strategy.HttpExecutor;
import com.zj.pipeline.executer.po.PipelineRecord;
import com.zj.pipeline.executer.po.TaskNodeRecord;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.NodeConfig;
import com.zj.pipeline.executer.vo.ExecuteParam;
import com.zj.pipeline.executer.vo.Stage;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.NodeRecordService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 流水线的执行支持按照每个阶段并行串行执行，执行过程中任意阶段执行失败，就表示整个流水线执行失败。\n 流水线包含必要的 开始和结束节点，中间则可以任意添加节点任务。每个任务节点支持添加任意类型的执行参数，
 * 比如创建时指定环境，比如运行指定 运行上限等。流水线可以添加等待节点（可以是执行等待或者是审批等待）。
 *
 * @author falcon
 * @since 2022/5/23
 */
@Slf4j
@Component
public class PipelineExecutor {

  private Executor executor = new ThreadPoolExecutor(5, 10, 3, TimeUnit.HOURS,
      new LinkedBlockingQueue<>(100), new CallerRunsPolicy());

  private final NodeRecordService nodeRecordService;

  private final Map<String, IRemoteInvoker> invokerMap;

  private final List<IPipelineInterceptor> interceptors;

  public PipelineExecutor(NodeRecordService nodeRecordService, List<IRemoteInvoker> invokers,
      List<IPipelineInterceptor> interceptors) {
    this.nodeRecordService = nodeRecordService;
    invokerMap = invokers.stream()
        .collect(Collectors.toMap(IRemoteInvoker::type, invoker -> invoker));
    this.interceptors = interceptors;
  }

  public void execute(ExecuteParam pipeline) {
    List<Stage> stages = pipeline.getStages();
    if (CollectionUtils.isEmpty(stages)) {
      log.info("stage list is empty");
      throw new IllegalArgumentException("stages is empty");
    }

    /*
     * 每执行一次任务，应该都有执行任务的记录。任务记录的最小单位是节点任务，所有任务的状态都是异步刷新。任务状态异步刷新
     * 那么则需要每个任务在添加时`，都明确配置查询状态的接口。
     * */
    log.info("start run pipeline={} name={}", pipeline.getPipelineId(), pipeline.getName());
    String recordId = UUID.randomUUID().toString();
    PipelineRecord pipelineRecord = PipelineRecord.builder().pipelineId(pipeline.getPipelineId())
        .recordId(recordId).build();
    nodeRecordService.savePipelineRecord(pipelineRecord);

    //todo 并行和串行执行
    stages.forEach(stage -> {
      log.info("start run stage={} name={}", stage.getStageId(), stage.getStageName());
      List<CompletableFuture<Void>> futures = stage.getNodeList().stream()
          .map(node -> CompletableFuture.runAsync(() -> runNodeTask(recordId, node), executor))
          .collect(Collectors.toList());

      log.info("run after nodes");
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}))
          .whenComplete((unused, throwable) -> {
            log.info("pipeline run complete pipelineId={}", pipeline.getPipelineId());
            if (Objects.nonNull(throwable)) {
              log.info("run error", throwable);
            }
          });
    });
  }

  private void runNodeTask(String recordId, TaskNode node) {
    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.before(node));
    }

    log.info("start run task recordId={}", recordId);
    String taskId = UUID.randomUUID().toString();
    int status = ProcessStatus.RUNNING.getType();
    TaskNodeRecord taskNodeRecord = TaskNodeRecord.builder().recordId(recordId)
        .taskId(taskId).status(status).build();
    nodeRecordService.saveTaskNodeRecord(taskNodeRecord);

    try {
      IRemoteInvoker remoteInvoker = invokerMap.get(node.getExecuteType());
      if (Objects.isNull(remoteInvoker)) {
        throw new RuntimeException("can not find remote invoker");
      }

      boolean executeFlag = remoteInvoker.execute(node.getRequestContext(), taskId);
      if (!executeFlag) {
        status = ProcessStatus.FAIL.getType();
        shutdownPipeline(recordId, node, taskNodeRecord);
      }
      log.info("task node run complete result={}", executeFlag);
    } catch (Exception e) {
      log.error("execute pipeline node error recordId={}", recordId, e);
      //如果请求失败则直接流水线终止
      status = ProcessStatus.FAIL.getType();
      shutdownPipeline(recordId, node, taskNodeRecord);
    }

    //保存node节点执行开始
    taskNodeRecord.setStatus(status);
    nodeRecordService.updateTaskNodeRecord(taskNodeRecord);

    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.after(node));
    }
  }

  private void shutdownPipeline(String recodeId, TaskNode node, TaskNodeRecord taskNodeRecord) {
    NodeConfig nodeConfig = node.getNodeConfig();
    if (nodeConfig.isIgnoreError()) {
      log.info("pipeline ignore error recordId={}", recodeId);
      taskNodeRecord.setStatus(ProcessStatus.IGNORE_FAIL.getType());
      nodeRecordService.updateTaskNodeRecord(taskNodeRecord);
      return;
    }

    log.info("shutdown pipeline recordId={}", recodeId);
  }


  public static void main(String[] args) {
    NodeRecordService nodeRecordService = new NodeRecordService();
    HttpExecutor httpExecutor = new HttpExecutor(nodeRecordService);
    PipelineExecutor pipelineExecutor = new PipelineExecutor(nodeRecordService,
        Collections.singletonList(httpExecutor), null);

    Stage stage = new Stage();
    stage.setStageId(UUID.randomUUID().toString());
    stage.setStageName("测试阶段");
    List<Stage> stages = new ArrayList<>();

    HttpRequestContext httpRequestContext = HttpRequestContext.builder().build();
    httpRequestContext.setUrl("http://www.baidu.com");
    httpRequestContext.setBody("{}");
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId("12344342342");
    taskNode.setName("执行节点");
    taskNode.setRequestContext(httpRequestContext);
    taskNode.setExecuteType(ExecuteType.HTTP.name());
    List<TaskNode> nodeList = new ArrayList<>();
    nodeList.add(taskNode);
    stage.setNodeList(nodeList);

    stages.add(stage);

    ExecuteParam pipeline = new ExecuteParam();
    pipeline.setPipelineId(UUID.randomUUID().toString());
    pipeline.setName("测试流水线");
    pipeline.setStages(stages);
    pipelineExecutor.execute(pipeline);
  }
}
