package com.zj.domain.entity.po.pipeline;

import com.zj.domain.entity.dto.pipeline.CompareResult;
import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Data
public class PipelineAction {

  private Long id;

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
  private String headers;

  /**
   * 执行请求url
   */
  private String actionUrl;

  /**
   * 执行请求参数
   */
  private String paramDetail;

  /**
   * 执行查询状态url
   */
  private String queryUrl;

  /**
   * 循环查询条件
   */
  private String queryExpression;

  /**
   * 查询结果比对条件
   */
  private String result;

  /**
   * 节点执行方式
   */
  private String executeType;

  private Long createTime;

  private Long updateTime;
}
