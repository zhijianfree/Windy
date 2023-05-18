package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.po.feature.TestCaseConfig;
import com.zj.domain.mapper.feeature.TestCaseConfigMapper;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class TestCaseConfigRepository extends
    ServiceImpl<TestCaseConfigMapper, TestCaseConfig> implements ITestCaseConfigRepository {

  @Override
  public List<TestCaseConfigDto> getCaseConfigs(String caseId) {
    List<TestCaseConfig> configs = list(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getUnionId, caseId));
    return OrikaUtil.convertList(configs, TestCaseConfigDto.class);
  }
}
