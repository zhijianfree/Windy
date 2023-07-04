package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Data
public class PublishBind {

  private Long id;

  private String publishId;

  private String userId;

  private String branch;

  private String serviceId;

  private String pipelineId;

  private String publishLine;

  private Integer status;

  private Long createTime;

  private Long updateTime;
}
