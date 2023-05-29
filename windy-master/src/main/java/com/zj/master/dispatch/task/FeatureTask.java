package com.zj.master.dispatch.task;

import com.zj.master.entity.vo.ExecuteContext;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Data
public class FeatureTask {

  /**
   * 用例任务的记录Id
   * */
  private String taskRecordId;

  private boolean isTempTask;

  /**
   * 用例任务Id
   * */
  private String taskId;

  /**
   * 这个Id是master执行记录ID，在崩溃恢复任务时使用
   * */
  private String logId;

  private LinkedBlockingQueue<String> featureIds;

  private ExecuteContext executeContext;

  public void addAll(List<String> features) {
    if (CollectionUtils.isEmpty(this.featureIds)) {
      this.featureIds = new LinkedBlockingQueue<>();
    }

    this.featureIds.addAll(features);
  }
}
