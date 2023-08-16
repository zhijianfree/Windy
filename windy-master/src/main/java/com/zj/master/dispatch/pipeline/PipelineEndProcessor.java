package com.zj.master.dispatch.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.master.dispatch.pipeline.listener.IPipelineEndListener;
import com.zj.master.entity.vo.NodeStatusChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 监听node节点变化，更新流水线状态
 *
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEndProcessor {

  private final IPipelineNodeRepository pipelineNodeRepository;
  private final INodeRecordRepository nodeRecordRepository;
  private final IDispatchLogRepository dispatchLogRepository;
  private final IPipelineHistoryRepository pipelineHistoryRepository;
  private final List<IPipelineEndListener> pipelineEndListeners;

  public PipelineEndProcessor(IPipelineNodeRepository pipelineNodeRepository,
      INodeRecordRepository nodeRecordRepository, IDispatchLogRepository dispatchLogRepository,
      IPipelineHistoryRepository pipelineHistoryRepository,
      List<IPipelineEndListener> pipelineEndListeners) {
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.nodeRecordRepository = nodeRecordRepository;
    this.dispatchLogRepository = dispatchLogRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.pipelineEndListeners = pipelineEndListeners;
  }

  public void statusChange(NodeStatusChange change) {
    log.info("receive pipeline notify historyId={}", change.getHistoryId());
    //1 如果是失败状态，那么直接更新流水线执行状态
    if (change.getProcessStatus().isFailStatus()) {
      pipelineHistoryRepository.updateStatus(change.getHistoryId(), change.getProcessStatus());
      dispatchLogRepository.updateLogStatus(change.getLogId(), change.getProcessStatus().getType());
      notifyPipelineEnd(change);
      return;
    }

    //2 节点运行成功状态，找到流水线历史下所有节点的执行记录
    List<NodeRecordDto> recordList = nodeRecordRepository.getRecordsByHistoryId(change.getHistoryId());
    List<String> recordNodeIds = recordList.stream()
        .filter(record -> ProcessStatus.isCompleteStatus(record.getStatus()))
        .map(NodeRecordDto::getNodeId).collect(Collectors.toList());

    //3 如果所有节点执行都是成功则流水线执行完成
    PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(change.getNodeId());
    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipelineNode.getPipelineId());
    boolean isAllComplete = pipelineNodes.stream().map(PipelineNodeDto::getNodeId)
        .allMatch(recordNodeIds::contains);
    if (isAllComplete) {
      log.info("pipeline run complete success historyId={}", change.getHistoryId());
      pipelineHistoryRepository.updateStatus(change.getHistoryId(), ProcessStatus.SUCCESS);
      dispatchLogRepository.updateLogStatus(change.getHistoryId(), ProcessStatus.SUCCESS.getType());
      notifyPipelineEnd(change);
    }
  }

  void notifyPipelineEnd(NodeStatusChange statusChange) {
    pipelineEndListeners.forEach(listener -> listener.handleEnd(statusChange));
  }

}
