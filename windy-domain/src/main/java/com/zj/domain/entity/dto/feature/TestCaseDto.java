package com.zj.domain.entity.dto.feature;

import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.po.feature.TestCase;
import java.util.Objects;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Data
public class TestCaseDto {

  /**
   * 测试用例集ID
   * */
  private String testCaseId;

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
}
