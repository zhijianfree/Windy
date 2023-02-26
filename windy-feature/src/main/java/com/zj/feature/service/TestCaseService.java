package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.dto.TestCaseDTO;
import com.zj.feature.entity.po.TestCase;
import com.zj.feature.mapper.TestCaseMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2022/12/12
 */
@Service
public class TestCaseService {

  @Autowired
  private TestCaseMapper testCaseMapper;

  public PageSize<TestCaseDTO> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
    IPage<TestCase> pageObj = testCaseMapper.selectPage(new Page<>(page, pageSize),
        Wrappers.lambdaQuery(TestCase.class).eq(TestCase::getServiceId, serviceId));

    List<TestCase> records = pageObj.getRecords();
    PageSize<TestCaseDTO> dtoPageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(records)) {
      dtoPageSize.setTotal(0);
      return dtoPageSize;
    }

    long total = pageObj.getTotal();
    dtoPageSize.setTotal(total);
    dtoPageSize.setData(
        records.stream().map(TestCaseDTO::toTestCaseDTO).collect(Collectors.toList()));
    return dtoPageSize;
  }

  public String createTestCase(TestCaseDTO testCaseDTO) {
    TestCase testCase = TestCaseDTO.toTestCase(testCaseDTO);
    String testCaseId = UUID.randomUUID().toString().replace("-", "");
    testCase.setTestCaseId(testCaseId);
    long dateNow = System.currentTimeMillis();
    testCase.setCreateTime(dateNow);
    testCase.setUpdateTime(dateNow);

    testCaseMapper.insert(testCase);
    return testCaseId;
  }

  public Integer updateTestCase(TestCaseDTO testCaseDTO) {
    TestCase testCase = TestCaseDTO.toTestCase(testCaseDTO);
    long dateNow = System.currentTimeMillis();
    testCase.setUpdateTime(dateNow);

    return testCaseMapper.update(testCase, Wrappers.lambdaUpdate(TestCase.class)
        .eq(TestCase::getTestCaseId, testCase.getTestCaseId()));
  }

  public TestCaseDTO getTestCase(String caseId) {
    TestCase testCase = testCaseMapper.selectOne(Wrappers.lambdaQuery(TestCase.class)
        .eq(TestCase::getTestCaseId, caseId));

    return TestCaseDTO.toTestCaseDTO(testCase);
  }

  public Integer deleteTestCase(String caseId) {
    return testCaseMapper.delete(Wrappers.lambdaQuery(TestCase.class)
        .eq(TestCase::getTestCaseId, caseId));
  }
}
