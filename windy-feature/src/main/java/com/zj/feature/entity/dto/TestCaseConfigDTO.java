package com.zj.feature.entity.dto;

import com.zj.feature.entity.po.TestCaseConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @author falcon
 * @since 2022/12/19
 */
@Data
public class TestCaseConfigDTO {

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

  public static TestCaseConfig toTestCaseConfig(TestCaseConfigDTO configDTO) {
    TestCaseConfig testCaseConfig = new TestCaseConfig();
    BeanUtils.copyProperties(configDTO, testCaseConfig);
    return testCaseConfig;
  }

  public static TestCaseConfigDTO toTestCaseConfigDTO(TestCaseConfig configDTO) {
    TestCaseConfigDTO testCaseConfig = new TestCaseConfigDTO();
    BeanUtils.copyProperties(configDTO, testCaseConfig);
    return testCaseConfig;
  }
}
