package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.po.feature.TestCase;
import com.zj.domain.mapper.feeature.TestCaseMapper;
import com.zj.domain.repository.feature.ITestCaseRepository;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/18
 */
@Repository
public class TestCaseRepository extends ServiceImpl<TestCaseMapper, TestCase> implements
    ITestCaseRepository {

  @Override
  public TestCaseDto getTestCaseByTaskId(String caseId) {
    TestCase testCase = getOne(
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getTestCaseId, caseId));
    return OrikaUtil.convert(testCase, TestCaseDto.class);
  }
}
