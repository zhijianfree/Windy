package com.zj.domain.entity.dto.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/19
 */
@Data
public class TestCaseConfigDto {

  private String configId;

  /**
   * 配置关联ID
   * */
  private String unionId;

  /**
   * 父节点ID
   * */
  private String parentId;

  /**
   * 节点类型
   * */
  private Integer type;

  /**
   * 参数Key
   * */
  private String paramKey;

  /**
   * 参数类型
   * */
  private String paramType;

  /**
   * 参数值
   * */
  private String value;

  /**
   * 排序
   * */
  private int sortOrder;

  private Long updateTime;

  private Long createTime;
}
