package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Data
public class PipelineActionDto {
  /**
   * 执行动作Id
   */
  private String actionId;

  /**
   * 执行动作名称
   */
  private String actionName;

  /**
   * 所属节点Id
   */
  private String nodeId;

  /**
   * 描述
   */
  private String description;

  /**
   * 请求的header
   */
  private Map<String, String> headers;

  /**
   * 触发执行请求url
   */
  private String actionUrl;

  /**
   * post请求方式
   */
  private String bodyType;

  /**
   * 触发任务的参数列表
   */
  private List<ActionParam> paramList;

  /**
   * 查询任务状态地址
   */
  private String queryUrl;

  private CompareResult loopExpression;

  /**
   * 查询结果状态比较逻辑
   */
  private List<CompareResult> compareResults;

  /**
   * 节点执行方式
   */
  private String executeType;

  private Long createTime;

  private Long updateTime;
}
