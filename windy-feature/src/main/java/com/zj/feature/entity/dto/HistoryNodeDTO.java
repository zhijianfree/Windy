package com.zj.feature.entity.dto;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/2/1
 */
@Data
public class HistoryNodeDTO {

  private String featureId;

  private String historyId;

  private String featureName;

  private String parentId;

  private List<HistoryNodeDTO> children;

  /**
   * 执行记录Id
   * */
  private String recordId;

  /**
   * 执行状态
   * */
  private Integer executeStatus;



}
