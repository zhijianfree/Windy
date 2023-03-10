package com.zj.feature.entity.dto;

import com.zj.feature.entity.po.TestCase;
import java.util.Objects;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @author falcon
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

    TestCaseDTO testCaseDTO = new TestCaseDTO();
    BeanUtils.copyProperties(testCase, testCaseDTO);

    return testCaseDTO;
  }

  public static TestCase toTestCase(TestCaseDTO testCaseDTO) {
    if (Objects.isNull(testCaseDTO)) {
      return null;
    }

    TestCase testCase = new TestCase();
    BeanUtils.copyProperties(testCaseDTO, testCase);
    return testCase;
  }
}
