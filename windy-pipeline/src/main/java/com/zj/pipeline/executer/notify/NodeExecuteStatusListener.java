package com.zj.pipeline.executer.notify;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.dto.PipelineHistoryDto;
import com.zj.pipeline.entity.dto.PipelineNodeDTO;
import com.zj.pipeline.entity.dto.PipelineStageDTO;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.entity.po.PipelineNode;
import com.zj.pipeline.executer.IStatusNotifyListener;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.service.PipelineHistoryService;
import com.zj.pipeline.service.PipelineNodeService;
import com.zj.pipeline.service.PipelineNodeRecordService;
import com.zj.pipeline.service.PipelineService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeExecuteStatusListener implements IStatusNotifyListener {

  @Autowired
  private PipelineNodeService pipelineNodeService;

  @Autowired
  private PipelineNodeRecordService nodeRecordService;

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  private PipelineHistoryService historyService;

  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive pipeline notify={}", JSON.toJSONString(event));
    //1 获取所有已经执行的节点记录
    NodeRecord nodeRecord = nodeRecordService.getNodeRecord(event.getRecordId());
    List<NodeRecord> recordList = nodeRecordService.list(Wrappers.lambdaQuery(NodeRecord.class)
        .eq(NodeRecord::getHistoryId, nodeRecord.getHistoryId()));
    List<String> recordNodeIds = recordList.stream()
        .filter(record -> ProcessStatus.isCompleteStatus(record.getStatus()))
        .map(NodeRecord::getNodeId).collect(Collectors.toList());

    //2 找到流水线下所有的节点
    PipelineNode pipelineNode = pipelineNodeService.getOne(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getNodeId, event.getNodeId()));
    String pipelineId = pipelineNode.getPipelineId();
    if (event.getProcessStatus().isFailStatus()) {
      historyService.updateStatus(nodeRecord.getHistoryId(), ProcessStatus.FAIL);
      return;
    }

    //如果都是成功则流水线执行完成
    PipelineDTO pipelineDetail = pipelineService.getPipelineDetail(pipelineId);
    boolean isAllComplete = pipelineDetail.getStageList().stream().map(PipelineStageDTO::getNodes)
        .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
        .map(PipelineNodeDTO::getNodeId).allMatch(recordNodeIds::contains);
    if (isAllComplete) {
      log.info("pipeline complete");
      historyService.updateStatus(nodeRecord.getHistoryId(), ProcessStatus.SUCCESS);
    }
  }
}
