package com.zj.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
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

}