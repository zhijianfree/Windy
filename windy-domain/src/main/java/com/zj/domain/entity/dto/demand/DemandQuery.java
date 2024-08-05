package com.zj.domain.entity.dto.demand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandQuery {
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
}
