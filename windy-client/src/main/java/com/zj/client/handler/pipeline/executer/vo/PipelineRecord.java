package com.zj.client.handler.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Data
@Builder
public class PipelineRecord {

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 流水线执行人
   * */
  private String userId;

  /**
   * 日志记录Id
   * */
  private String historyId;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 更新时间
   * */
  private Long updateTime;


  private Integer pipelineStatus;

}
