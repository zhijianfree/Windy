package com.zj.feature.entity.po;

import lombok.Data;

/**
 * @author falcon
 * @since 2022/12/19
 */
@Data
public class TestCaseConfig {

  private Long id;

  private String configId;

  /**
   * 测试集ID
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
