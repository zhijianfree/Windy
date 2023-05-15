package com.zj.domain.entity.dto.pipeline;
import java.util.List;
import lombok.Data;

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
   * 用户Id
   */
  private String userId;

  /**
   * 描述
   */
  private String description;

  /**
   * 执行请求url
   */
  private String actionUrl;
  private List<ActionParam> paramList;
  private String queryUrl;
  private List<CompareResult> compareResults;

  /**
   * 节点执行方式
   */
  private String executeType;
  private Long createTime;
  private Long updateTime;
}
