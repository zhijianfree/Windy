package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Data
public class PublishBindDto {

  private String publishId;

  private String branch;

  private String serviceId;

  private String message;

  private String pipelineId;

  private Integer status;

  private Long createTime;

  private Long updateTime;
}
