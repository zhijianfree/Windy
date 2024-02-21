package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.uuid.UniqueIdService;
import com.zj.common.model.DispatchTaskModel;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.dto.log.SubDispatchLogDto;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.pipeline.builder.RefreshContextBuilder;
import com.zj.master.dispatch.pipeline.builder.RequestContextBuilder;
import com.zj.master.entity.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Component
public class PipelineDispatch implements IDispatchExecutor {

  private final IPipelineRepository pipelineRepository;
  private final IPipelineNodeRepository pipelineNodeRepository;
  private final IPipelineHistoryRepository pipelineHistoryRepository;
  private final IPipelineActionRepository pipelineActionRepository;
  private final UniqueIdService uniqueIdService;
  private final PipelineExecuteProxy pipelineExecuteProxy;
  private final ISubDispatchLogRepository subDispatchLogRepository;
  private final IDispatchLogRepository dispatchLogRepository;

  public PipelineDispatch(IPipelineRepository pipelineRepository,
      IPipelineNodeRepository pipelineNodeRepository,
      IPipelineHistoryRepository pipelineHistoryRepository,
      IPipelineActionRepository pipelineActionRepository, UniqueIdService uniqueIdService,
      PipelineExecuteProxy pipelineExecuteProxy, ISubDispatchLogRepository subDispatchLogRepository,
      IDispatchLogRepository dispatchLogRepository) {
    this.pipelineRepository = pipelineRepository;
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.pipelineActionRepository = pipelineActionRepository;
    this.uniqueIdService = uniqueIdService;
    this.pipelineExecuteProxy = pipelineExecuteProxy;
    this.subDispatchLogRepository = subDispatchLogRepository;
    this.dispatchLogRepository = dispatchLogRepository;
  }

  @Override
  public LogType type() {
    return LogType.PIPELINE;
  }

  @Override
  public boolean isExistInJvm(DispatchLogDto taskLog) {
    return pipelineExecuteProxy.isExitTask(taskLog.getSourceRecordId());
  }

  @Override
  public String dispatch(DispatchTaskModel task, String logId) {
    PipelineDto pipeline = pipelineRepository.getPipeline(task.getSourceId());
    if (Objects.isNull(pipeline)) {
      log.info("can not find pipeline name={} pipelineId={}", task.getSourceName(),
          task.getSourceId());
      return null;
    }

    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", task.getSourceName(),
          task.getSourceId());
      return null;
    }

    String historyId = uniqueIdService.getUniqueId();
    saveHistory(pipeline.getPipelineId(), historyId);
    log.info("start run pipeline={} name={} historyId={}", task.getSourceId(), task.getSourceName(),
        historyId);

    dispatchLogRepository.updateLogSourceRecord(logId, historyId);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(historyId);
    pipelineTask.setLogId(logId);

    List<TaskNode> taskNodeList = pipelineNodes.stream()
        .sorted(Comparator.comparing(PipelineNodeDto::getSortOrder))
        .map(node -> buildTaskNode(node, historyId, pipeline.getServiceId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    pipelineTask.addAll(taskNodeList);
    Map<String, String> executeTypeMap = taskNodeList.stream()
        .collect(Collectors.toMap(TaskNode::getNodeId, TaskNode::getExecuteType));

    createSubTaskLog(pipelineNodes, pipelineTask.getLogId(), executeTypeMap);
    pipelineExecuteProxy.runTask(pipelineTask);
    return historyId;
  }

  private void createSubTaskLog(List<PipelineNodeDto> pipelineNodes, String logId,
      Map<String, String> executeTypeMap) {
    List<SubDispatchLogDto> logList = pipelineNodes.stream().map(pipelineNode -> {
      SubDispatchLogDto subTaskLog = new SubDispatchLogDto();
      subTaskLog.setSubTaskId(uniqueIdService.getUniqueId());
      subTaskLog.setSubTaskName(pipelineNode.getNodeName());
      subTaskLog.setLogId(logId);
      subTaskLog.setExecuteId(pipelineNode.getNodeId());
      subTaskLog.setExecuteType(executeTypeMap.get(pipelineNode.getNodeId()));
      subTaskLog.setStatus(ProcessStatus.RUNNING.getType());
      long dateNow = System.currentTimeMillis();
      subTaskLog.setCreateTime(dateNow);
      subTaskLog.setUpdateTime(dateNow);
      return subTaskLog;
    }).collect(Collectors.toList());
    subDispatchLogRepository.batchSaveLogs(logList);
  }

  private void saveHistory(String pipelineId, String historyId) {
    PipelineHistoryDto pipelineHistory = new PipelineHistoryDto();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineId(pipelineId);
    pipelineHistory.setPipelineStatus(ProcessStatus.RUNNING.getType());
    pipelineHistory.setPipelineConfig("");
    pipelineHistory.setBranch("master");
    pipelineHistory.setExecutor("admin");
    pipelineHistory.setCreateTime(System.currentTimeMillis());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    pipelineHistoryRepository.createPipelineHistory(pipelineHistory);
  }


  private TaskNode buildTaskNode(PipelineNodeDto pipelineNode, String historyId, String serviceId) {
    log.info("start build taskNode ={}", JSON.toJSONString(pipelineNode));
    TaskNode taskNode = new TaskNode();
    taskNode.setNodeId(pipelineNode.getNodeId());
    taskNode.setName(pipelineNode.getNodeName());
    taskNode.setExecuteTime(System.currentTimeMillis());
    taskNode.setHistoryId(historyId);
    taskNode.setServiceId(serviceId);

    ConfigDetail configDetail = JSON.parseObject(pipelineNode.getConfigDetail(),
        ConfigDetail.class);
    PipelineActionDto action = pipelineActionRepository.getAction(configDetail.getActionId());
    if (Objects.isNull(action)) {
      return null;
    }
    taskNode.setExecuteType(action.getExecuteType());

    ActionDetail actionDetail = new ActionDetail(configDetail, action);
    RequestContext requestContext = RequestContextBuilder.createContext(actionDetail);
    requestContext.setPipelineId(pipelineNode.getPipelineId());
    taskNode.setRequestContext(requestContext);

    RefreshContext refreshContext = RefreshContextBuilder.createContext(actionDetail);
    taskNode.setRefreshContext(refreshContext);

    NodeConfig nodeConfig = new NodeConfig();
    taskNode.setNodeConfig(nodeConfig);
    return taskNode;
  }

  @Override
  public boolean resume(DispatchLogDto dispatchLog) {
    String pipelineId = dispatchLog.getSourceId();
    PipelineDto pipeline = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      log.info("resume task not find pipeline name={} pipelineId={}", dispatchLog.getSourceName(),
          pipelineId);
      return false;
    }

    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipeline.getPipelineId());
    if (CollectionUtils.isEmpty(pipelineNodes)) {
      log.info("can not find pipeline nodes name={} pipelineId={}", dispatchLog.getSourceName(),
          pipelineId);
      return false;
    }

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setLogId(dispatchLog.getLogId());
    if (StringUtils.isBlank(dispatchLog.getSourceRecordId())) {
      String historyId = uniqueIdService.getUniqueId();
      saveHistory(pipelineId, historyId);
      dispatchLog.setSourceRecordId(historyId);
    }

    //过滤掉已经执行完成的任务
    List<SubDispatchLogDto> subLogs = subDispatchLogRepository.getSubLogByLogId(
        dispatchLog.getLogId());
    List<String> subTasks = subLogs.stream()
        .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
        .map(SubDispatchLogDto::getExecuteId).collect(Collectors.toList());
    List<TaskNode> taskNodeList = pipelineNodes.stream()
        .filter(node -> subTasks.contains(node.getNodeId()))
        .map(node -> buildTaskNode(node, dispatchLog.getSourceRecordId(), pipeline.getServiceId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    pipelineTask.setHistoryId(dispatchLog.getSourceRecordId());
    pipelineTask.addAll(taskNodeList);

    if (CollectionUtils.isEmpty(subLogs)) {
      //如果找不到子任务执行记录，那么就需要重新创建
      Map<String, String> executeTypeMap = taskNodeList.stream()
          .collect(Collectors.toMap(TaskNode::getNodeId, TaskNode::getExecuteType));
      createSubTaskLog(pipelineNodes, dispatchLog.getLogId(), executeTypeMap);
    }
    pipelineExecuteProxy.runTask(pipelineTask);
    return true;
  }

  @Override
  public Integer getExecuteCount() {
    return pipelineExecuteProxy.getTaskSize();
  }
}
