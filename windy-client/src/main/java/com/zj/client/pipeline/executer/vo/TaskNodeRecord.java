package com.zj.client.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Data
@Builder
public class TaskNodeRecord {

  private String recordId;

  private String historyId;

  private String nodeId;

  private Integer status;

  private Integer code;

  private String result;

  private Long createTime;

  private Long updateTime;

}
