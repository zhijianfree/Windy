package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.feature.TestCaseDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseRepository {

    TestCaseDto getTestCaseById(String caseId);

    boolean saveCase(TestCaseDto testCaseDto);

    Boolean updateCase(TestCaseDto testCaseDto);

    Boolean deleteTestCase(String caseId);

    List<TestCaseDto> getServiceCases(String serviceId);

    IPage<TestCaseDto> getCasePage(String serviceId, Integer page, Integer pageSize);

    IPage<TestCaseDto> getE2ECasesPage(Integer page, Integer pageSize);

    List<TestCaseDto> getE2ECases();
}
