package com.zj.pipeline.entity.po;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/27
 */
@Data
public class PipelineAction {

  private Long id;
  private String actionId;
  private String actionName;
  private String nodeId;
  private String userId;
  private String description;
  private String actionUrl;
  private String paramDetail;
  private String queryUrl;
  private String result;
  private Long createTime;
  private Long updateTime;
}
