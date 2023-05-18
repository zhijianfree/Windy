package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.po.feature.TestCase;
import com.zj.domain.mapper.feeature.TestCaseMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Service
public class TestCaseService extends ServiceImpl<TestCaseMapper, TestCase> {

  @Autowired
  private UniqueIdService uniqueIdService;

  public PageSize<TestCaseDto> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
    IPage<TestCase> pageObj = page(new Page<>(page, pageSize),
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getServiceId, serviceId));

    List<TestCase> records = pageObj.getRecords();
    PageSize<TestCaseDto> dtoPageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(records)) {
      dtoPageSize.setTotal(0);
      return dtoPageSize;
    }

    long total = pageObj.getTotal();
    dtoPageSize.setTotal(total);
    dtoPageSize.setData(
        records.stream().map(TestCaseDto::toTestCaseDTO).collect(Collectors.toList()));
    return dtoPageSize;
  }

  public String createTestCase(TestCaseDto testCaseDTO) {
    TestCase testCase = TestCaseDto.toTestCase(testCaseDTO);
    String testCaseId = uniqueIdService.getUniqueId();
    testCase.setTestCaseId(testCaseId);
    long dateNow = System.currentTimeMillis();
    testCase.setCreateTime(dateNow);
    testCase.setUpdateTime(dateNow);
    save(testCase);
    return testCaseId;
  }

  public Boolean updateTestCase(TestCaseDto testCaseDTO) {
    TestCase testCase = TestCaseDto.toTestCase(testCaseDTO);
    long dateNow = System.currentTimeMillis();
    testCase.setUpdateTime(dateNow);

    return update(testCase, Wrappers.lambdaUpdate(TestCase.class)
        .eq(TestCase::getTestCaseId, testCase.getTestCaseId()));
  }

  public TestCaseDto getTestCase(String caseId) {
    TestCase testCase = getOne(Wrappers.lambdaQuery(TestCase.class)
        .eq(TestCase::getTestCaseId, caseId));
    return TestCaseDto.toTestCaseDTO(testCase);
  }

  public Boolean deleteTestCase(String caseId) {
    return remove(Wrappers.lambdaQuery(TestCase.class)
        .eq(TestCase::getTestCaseId, caseId));
  }
}
