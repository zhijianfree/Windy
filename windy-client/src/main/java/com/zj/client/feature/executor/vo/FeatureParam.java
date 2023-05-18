package com.zj.client.feature.executor.vo;

import com.zj.client.entity.po.ExecutePoint;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Data
public class FeatureParam {
  /**
   * 用例的执行点列表，要将这个参数传递过来，
   * 这样设计就可以保证client端最轻量化，不连接数据库，只做任务执行
   * */
  private List<ExecutePoint> executePointList;
  /**
   * 用例ID
   * */
  private String featureId;

  /**
   * 任务Id
   * */
  private String taskRecordId;

  /**
   * 用例执行上下文，也就是测试集或者任务的全局变量
   * */
  private Map<String, Object> executeContext;

  /**
   * 分配任务的masterIP
   * */
  private String masterIp;
}
