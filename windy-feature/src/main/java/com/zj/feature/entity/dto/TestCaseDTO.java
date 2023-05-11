package com.zj.feature.entity.dto;

import com.zj.common.utils.OrikaUtil;
import com.zj.feature.entity.po.TestCase;
import java.util.Objects;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Data
public class TestCaseDTO {

  /**
   * 测试用例集ID
   * */
  private String testCaseId;

  /**
   * 创建人
   * */
  private String author;

  /**
   * 服务ID
   * */
  private String serviceId;

  /**
   * 用例集名称
   * */
  private String testCaseName;

  /**
   * 用例集描述
   * */
  private String description;

  private Long createTime;

  private Long updateTime;

  public static TestCaseDTO toTestCaseDTO(TestCase testCase) {
    if (Objects.isNull(testCase)) {
      return null;
    }
    return OrikaUtil.convert(testCase, TestCaseDTO.class);
  }

  public static TestCase toTestCase(TestCaseDTO testCaseDTO) {
    if (Objects.isNull(testCaseDTO)) {
      return null;
    }
    return OrikaUtil.convert(testCaseDTO, TestCase.class);
  }
}
