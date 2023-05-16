package com.zj.client.feature.executor.vo;

import com.zj.client.entity.po.ExecutePoint;
import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Data
public class FeatureParam {
  /**
   * 用例的执行点列表，要将这个参数传递过来，
   * 这样设计就可以保证client端最轻量化，不连接数据库，只做任务执行
   * */
  List<ExecutePoint> executePointList;
  /**
   * 用例ID
   * */
  String featureId;

  /**
   * 任务Id
   * */
  String taskId;

  /**
   * 用例执行上下文，也就是测试集或者任务的全局变量
   * */
  ExecuteContext executeContext;
}
