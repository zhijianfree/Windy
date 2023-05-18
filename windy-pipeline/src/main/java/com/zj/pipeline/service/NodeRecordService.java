package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.mapper.pipeline.NodeRecordMapper;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService extends ServiceImpl<NodeRecordMapper, NodeRecord> {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  public static final String APPROVAL_TIPS = "审核通过";

  public void updateNodeRecordStatus(String recordId, Integer type, String message) {
    boolean update = nodeRecordRepository.updateNodeRecordStatus(recordId, type, message);
    log.info("update result={}", update);
  }

  public Boolean approval(String historyId, String nodeId) {
    NodeRecord nodeRecord = getOne(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getNodeId, nodeId));
    log.info("get approval recordId={}", nodeRecord.getNodeId());
    updateNodeRecordStatus(nodeRecord.getRecordId(), ProcessStatus.SUCCESS.getType(),
        JSON.toJSONString(Collections.singletonList(APPROVAL_TIPS)));
    return true;
  }
}
