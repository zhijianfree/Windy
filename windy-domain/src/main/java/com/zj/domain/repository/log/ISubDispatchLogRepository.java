package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISubDispatchLogRepository {

  /**
   * 批量保存日志
   * @param subTaskLogs 日志列表
   */
  void batchSaveLogs(List<SubDispatchLogBO> subTaskLogs);

  /**
   * 更新日志状态
   * @param logId 日志ID
   * @param executeId 执行ID
   * @param status 状态
   */
  void updateLogStatus(String logId, String executeId, Integer status);

  /**
   * 获取日志
   * @param logId 日志ID
   * @return 日志信息
   */
  List<SubDispatchLogBO> getSubLogByLogId(String logId);

  /**
   * 批量删除日志
   * @param logIds 日志ID列表
   */
  void batchDeleteByLogIds(List<String> logIds);

  /**
   * 获取子任务记录日志
   * @param logId 日志ID
   * @param executeId 执行ID
   * @return 日志信息
   */
  SubDispatchLogBO getSubDispatchLog(String logId, String executeId);

  /**
   * 更新子任务日志客户端IP
   * @param logId 日志ID
   * @param nodeId 节点ID
   * @param clientIp 客户端IP
   */
  void updateSubLogClientIp(String logId, String nodeId, String clientIp);
}
