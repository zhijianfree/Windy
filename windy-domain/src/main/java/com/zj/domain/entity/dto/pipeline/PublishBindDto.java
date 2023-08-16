package com.zj.domain.entity.dto.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Data
public class PublishBindDto {

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
