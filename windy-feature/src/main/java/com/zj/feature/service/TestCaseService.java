package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.repository.feature.ITestCaseRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Service
public class TestCaseService {

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private ITestCaseRepository testCaseRepository;

  public PageSize<TestCaseDto> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
    IPage<TestCaseDto> pageObj = testCaseRepository.getCasePage(serviceId, page, pageSize);
    List<TestCaseDto> records = pageObj.getRecords();
    PageSize<TestCaseDto> dtoPageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(records)) {
      dtoPageSize.setTotal(0);
      return dtoPageSize;
    }

    long total = pageObj.getTotal();
    dtoPageSize.setTotal(total);
    dtoPageSize.setData(records);
    return dtoPageSize;
  }

  public String createTestCase(TestCaseDto testCaseDto) {
    String testCaseId = uniqueIdService.getUniqueId();
    testCaseDto.setTestCaseId(testCaseId);
    testCaseRepository.saveCase(testCaseDto);
    return testCaseId;
  }

  public Boolean updateTestCase(TestCaseDto testCaseDto) {
    long dateNow = System.currentTimeMillis();
    testCaseDto.setUpdateTime(dateNow);
    return testCaseRepository.updateCase(testCaseDto);
  }

  public TestCaseDto getTestCase(String caseId) {
    return testCaseRepository.getTestCaseById(caseId);
  }

  public Boolean deleteTestCase(String caseId) {
    return testCaseRepository.deleteTestCase(caseId);
  }
}
