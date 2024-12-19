package com.zj.domain.entity.bo.demand;

import com.zj.domain.entity.vo.BaseQuery;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandQueryBO extends BaseQuery {
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
   * 用户ID
   */
  private String proposer;

  /**
   * 迭代ID
   */
  private String iterationId;

  /**
   * 空间ID
   */
  private String spaceId;

  /**
   * 处理人Id
   */
  private String acceptor;
}
