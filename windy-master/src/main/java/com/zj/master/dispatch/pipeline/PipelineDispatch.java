package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
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
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.master.entity.vo.ConfigDetail;
import com.zj.master.entity.vo.NodeConfig;
import com.zj.master.entity.vo.RefreshContext;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Component
public class PipelineDispatch implements IDispatchExecutor {

  @Autowired
  private IPipelineRepository pipelineRepository;

  @Autowired
  private IPipelineNodeRepository pipelineNodeRepository;

  @Autowired
  private IPipelineHistoryRepository pipelineHistoryRepository;

  @Autowired
  private IPipelineActionRepository pipelineActionRepository;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private PipelineExecuteProxy pipelineExecuteProxy;

  @Autowired
  private ISubDispatchLogRepository subDispatchLogRepository;

  @Autowired
  private IDispatchLogRepository dispatchLogRepository;

  @Override
  public Integer type() {
    return LogType.PIPELINE.getType();
  }

  @Override
  public boolean isExitInJvm(DispatchLogDto taskLog) {
    return pipelineExecuteProxy.isExitTask(taskLog.getSourceRecordId());
  }

  @Override
  public String dispatch(TaskDetailDto task) {
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

    dispatchLogRepository.updateLogSourceRecord(task.getTaskLogId(), historyId);

    PipelineTask pipelineTask = new PipelineTask();
    pipelineTask.setPipelineId(pipeline.getPipelineId());
    pipelineTask.setHistoryId(historyId);
    pipelineTask.setLogId(task.getTaskLogId());

    List<TaskNode> taskNodeList = pipelineNodes.stream()
        .map(node -> buildTaskNode(node, historyId, pipeline.getServiceId()))
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
}
