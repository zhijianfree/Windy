package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/15
 */
public interface IPipelineHistoryRepository {

  PipelineHistoryDto getPipelineHistory(String historyId);

  boolean createPipelineHistory(PipelineHistoryDto pipelineHistoryDto);

  List<PipelineHistoryDto> listPipelineHistories(String pipelineId);

  PipelineHistoryDto getLatestPipelineHistory(String pipelineId);

  boolean updateStatus(String historyId, ProcessStatus processStatus);
}
