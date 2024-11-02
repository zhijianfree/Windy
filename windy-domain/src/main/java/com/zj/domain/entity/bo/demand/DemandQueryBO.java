package com.zj.domain.entity.bo.demand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandQueryBO {
  /**
   * 需求状态
   */
  private Integer status;

  /**
   * 分页数
   */
  private Integer page;

  /**
   * 页大小
   */
  private Integer pageSize;

  /**
   * 需求名称
   */
  private String name;

  /**
   * 创建人
   */
  private String creator;

  /**
   * 迭代ID
   */
  private String iterationId;

  /**
   * 空间ID
   */
  private String spaceId;
}
