package com.zj.master.dispatch.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听node节点变化，更新流水线状态
 *
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEndProcessor {

  @Autowired
  private IPipelineNodeRepository pipelineNodeRepository;

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private IPipelineRepository pipelineRepository;

  @Autowired
  private IPipelineHistoryRepository pipelineHistoryRepository;

  public void statusChange(String historyId, String nodeId, ProcessStatus processStatus) {
    log.info("receive pipeline notify historyId={}", historyId);
    //1 如果是失败状态，那么直接更新流水线执行状态
    if (processStatus.isFailStatus()) {
      pipelineHistoryRepository.updateStatus(historyId, processStatus);
      return;
    }

    //2 节点运行成功状态，找到流水线历史下所有节点的执行记录
    List<NodeRecord> recordList = nodeRecordRepository.getRecordsByHistoryId(historyId);
    List<String> recordNodeIds = recordList.stream()
        .filter(record -> ProcessStatus.isCompleteStatus(record.getStatus()))
        .map(NodeRecord::getNodeId).collect(Collectors.toList());

    //3 如果所有节点执行都是成功则流水线执行完成
    PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(nodeId);
    List<PipelineNodeDto> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
        pipelineNode.getPipelineId());
    boolean isAllComplete = pipelineNodes.stream().map(PipelineNodeDto::getNodeId)
        .allMatch(recordNodeIds::contains);
    if (isAllComplete) {
      log.info("pipeline run complete success historyId={}", historyId);
      pipelineHistoryRepository.updateStatus(historyId, ProcessStatus.SUCCESS);
    }
  }
}
