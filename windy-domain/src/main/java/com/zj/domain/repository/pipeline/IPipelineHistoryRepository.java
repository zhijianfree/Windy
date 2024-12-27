package com.zj.domain.repository.pipeline;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineHistoryRepository {

  /**
   * 根据历史ID获取详情
   * @param historyId 历史ID
   * @return 历史信息
   */
  PipelineHistoryBO getPipelineHistory(String historyId);

  /**
   * 创建流水线历史
   * @param pipelineHistoryBO 历史信息
   * @return 是否成功
   */
  boolean createPipelineHistory(PipelineHistoryBO pipelineHistoryBO);

  /**
   * 查询流水线历史记录列表
   * @param pipelineId 流水线ID
   * @return 历史记录列表
   */
  List<PipelineHistoryBO> listPipelineHistories(String pipelineId);

  /**
   * 获取最新的流水线历史
   * @param pipelineId 流水线ID
   * @return 历史信息
   */
  PipelineHistoryBO getLatestPipelineHistory(String pipelineId);

  /**
   * 更新流水线历史状态
   * @param historyId 历史ID
   * @param processStatus 状态
   * @return 是否成功
   */
  boolean updateStatus(String historyId, ProcessStatus processStatus);

  /**
   * 获取旧的流水线历史
   * @param queryTime 查询时间
   * @return 历史列表
   */
  List<PipelineHistoryBO> getOldPipelineHistory(long queryTime);

  /**
   * 删除流水线历史
   * @param historyId 历史ID
   * @return 是否成功
   */
  boolean deleteByHistoryId(String historyId);

  boolean deleteByPipelineId(String pipelineId);
}
