package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.enums.CaseType;
import com.zj.domain.entity.po.feature.TestCase;
import com.zj.domain.mapper.feeature.TestCaseMapper;
import com.zj.domain.repository.feature.ITestCaseRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class TestCaseRepository extends ServiceImpl<TestCaseMapper, TestCase> implements
    ITestCaseRepository {

  @Override
  public TestCaseDto getTestCaseById(String caseId) {
    TestCase testCase = getOne(
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getTestCaseId, caseId));
    return OrikaUtil.convert(testCase, TestCaseDto.class);
  }

  @Override
  public boolean saveCase(TestCaseDto testCaseDto) {
    TestCase testCase = OrikaUtil.convert(testCaseDto, TestCase.class);
    long dateNow = System.currentTimeMillis();
    testCase.setCreateTime(dateNow);
    testCase.setUpdateTime(dateNow);
    return save(testCase);
  }

  @Override
  public Boolean updateCase(TestCaseDto testCaseDto) {
    TestCase testCase = OrikaUtil.convert(testCaseDto, TestCase.class);
    testCase.setUpdateTime(System.currentTimeMillis());
    return update(testCase, Wrappers.lambdaUpdate(TestCase.class)
        .eq(TestCase::getTestCaseId, testCase.getTestCaseId()));
  }

  @Override
  public Boolean deleteTestCase(String caseId) {
    return remove(Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getTestCaseId, caseId));
  }

  @Override
  public List<TestCaseDto> getServiceCases(String serviceId) {
    List<TestCase> testCases = list(
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getServiceId, serviceId));
    return OrikaUtil.convertList(testCases, TestCaseDto.class);
  }

  @Override
  public IPage<TestCaseDto> getCasePage(String serviceId, Integer page, Integer pageSize) {
    IPage<TestCase> pageObj = page(new Page<>(page, pageSize),
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getServiceId, serviceId));

    IPage<TestCaseDto> caseDtoPage = new Page<>();
    caseDtoPage.setTotal(pageObj.getTotal());
    caseDtoPage.setRecords(OrikaUtil.convertList(pageObj.getRecords(), TestCaseDto.class));
    return caseDtoPage;
  }

  @Override
  public IPage<TestCaseDto> getE2ECasesPage(Integer page, Integer pageSize) {
    IPage<TestCase> pageObj = page(new Page<>(page, pageSize),
            Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getCaseType, CaseType.E2E.getType()));

    IPage<TestCaseDto> caseDtoPage = new Page<>();
    caseDtoPage.setTotal(pageObj.getTotal());
    caseDtoPage.setRecords(OrikaUtil.convertList(pageObj.getRecords(), TestCaseDto.class));
    return caseDtoPage;
  }

  @Override
  public List<TestCaseDto> getE2ECases() {
    List<TestCase> testCases = list(
            Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getCaseType, CaseType.E2E.getType()));
    return OrikaUtil.convertList(testCases, TestCaseDto.class);
  }
}
