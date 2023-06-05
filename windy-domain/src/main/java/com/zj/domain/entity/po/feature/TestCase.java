package com.zj.domain.entity.po.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Data
public class TestCase {

  private Long id;

  /**
   * 测试用例集ID
   * */
  private String testCaseId;

  /**
   * 创建人
   * */
  private String author;

  /**
   * 用例集名称
   * */
  private String testCaseName;

  /**
   * 用例集描述
   * */
  private String description;

  /**
   * 服务ID
   * */
  private String serviceId;

  private Long createTime;

  private Long updateTime;
}
