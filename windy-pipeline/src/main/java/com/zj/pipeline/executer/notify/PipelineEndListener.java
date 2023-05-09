package com.zj.pipeline.executer.notify;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.dto.PipelineNodeDTO;
import com.zj.pipeline.entity.dto.PipelineStageDTO;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.entity.po.PipelineNode;
import com.zj.pipeline.executer.IStatusNotifyListener;
import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.PipelineHistoryService;
import com.zj.pipeline.service.NodeRecordService;
import com.zj.pipeline.service.PipelineNodeService;
import com.zj.pipeline.service.PipelineService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听node节点变化，更新流水线状态
 *
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class PipelineEndListener implements IStatusNotifyListener {

  @Autowired
  private PipelineNodeService pipelineNodeService;

  @Autowired
  private NodeRecordService nodeRecordService;

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  private PipelineHistoryService historyService;

  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive pipeline notify={}", JSON.toJSONString(event));
    //1 如果是失败状态，那么直接更新流水线执行状态
    TaskNode taskNode = event.getTaskNode();
    if (event.getProcessStatus().isFailStatus()) {
      historyService.updateStatus(taskNode.getHistoryId(), event.getProcessStatus());
      return;
    }

    //2 节点运行成功状态，找到流水线历史下所有节点的执行记录
    List<NodeRecord> recordList = nodeRecordService.list(Wrappers.lambdaQuery(NodeRecord.class)
        .eq(NodeRecord::getHistoryId, taskNode.getHistoryId()));
    List<String> recordNodeIds = recordList.stream()
        .filter(record -> ProcessStatus.isCompleteStatus(record.getStatus()))
        .map(NodeRecord::getNodeId).collect(Collectors.toList());

    //3 如果所有节点执行都是成功则流水线执行完成
    PipelineNode pipelineNode = pipelineNodeService.getOne(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getNodeId, taskNode.getNodeId()));

    PipelineDTO pipelineDetail = pipelineService.getPipelineDetail(pipelineNode.getPipelineId());
    boolean isAllComplete = pipelineDetail.getStageList().stream().map(PipelineStageDTO::getNodes)
        .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
        .map(PipelineNodeDTO::getNodeId).allMatch(recordNodeIds::contains);
    if (isAllComplete) {
      log.info("pipeline run complete success historyId={}", taskNode.getHistoryId());
      historyService.updateStatus(taskNode.getHistoryId(), ProcessStatus.SUCCESS);
    }
  }
}
