package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.DispatchLogBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface IDispatchLogRepository {

  /**
   * 获取运行中的事务任务
   * @return 任务列表
   */
  List<DispatchLogBO> getRunningDispatchLog();

  /**
   * 删除7天内的日志
   * @return 任务列表
   */
  List<String> delete7DayLog();

  /**
   * 保存日志
   * @param taskLog 日志信息
   */
  void saveLog(DispatchLogBO taskLog);

  /**
   * 更新日志状态
   * @param logId 日志ID
   * @param type 状态
   */
  boolean updateLogStatus(String logId, int type);

  /**
   * 更新日志主节点IP
   * @param logId 日志ID
   * @param localIP 本地IP
   * @param lockVersion 乐观锁版本
   * @return 是否成功
   */
  boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion);

  /**
   * 更新日志源记录
   * @param logId 日志ID
   * @param recordId 记录ID
   */
  void updateLogSourceRecord(String logId, String recordId);

  /**
   * 获取日志
   * @param logId 日志ID
   * @return 日志信息
   */
  DispatchLogBO getDispatchLog(String logId);
}
