package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineHistoryRepository {

  PipelineHistoryBO getPipelineHistory(String historyId);

  boolean createPipelineHistory(PipelineHistoryBO pipelineHistoryBO);

  List<PipelineHistoryBO> listPipelineHistories(String pipelineId);

  PipelineHistoryBO getLatestPipelineHistory(String pipelineId);

  boolean updateStatus(String historyId, ProcessStatus processStatus);
}
